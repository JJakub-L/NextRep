package com.example.nextrep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextrep.data.local.WorkoutDao
import com.example.nextrep.domain.logic.ScoringStrategy
import com.example.nextrep.domain.logic.VolumeStrategy
import com.example.nextrep.domain.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class WorkoutViewModel(private val dao: WorkoutDao) : ViewModel() {

    val workoutPlans: StateFlow<List<Workout>> = dao.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val streak: StateFlow<Int> = workoutPlans
        .map { workouts -> calculateStreak(workouts) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val progressCards: StateFlow<List<TrainingProgressCard>> = dao.getAllPlans()
        .map { allWorkouts ->
            val completed = allWorkouts.filter { it.isCompleted && it.completionDate != null }
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            completed.groupBy { it.name }.map { (name, history) ->
                val latestWorkout = history.maxBy { it.completionDate!! }
                
                val exerciseComparisons = latestWorkout.exercises.map { exercise ->
                    ExerciseComparison(
                        exerciseName = exercise.name,
                        todayMax = getBestVolumeInPeriod(exercise.name, allWorkouts, dayStart(now), now),
                        weekAgoMax = getBestVolumeInPeriod(
                            exercise.name,
                            allWorkouts,
                            dayStart(now - 7 * dayMs),
                            dayStart(now) - 1
                        ),
                        monthAgoMax = getBestVolumeInPeriod(
                            exercise.name,
                            allWorkouts,
                            dayStart(now - 30 * dayMs),
                            dayStart(now - 8 * dayMs)
                        )
                    )
                }
                TrainingProgressCard(trainingName = name, exercises = exerciseComparisons)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyChartData: StateFlow<List<WeeklyChartPoint>> = dao.getAllPlans()
        .map { allWorkouts ->
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val calendar = Calendar.getInstance()
            val polishDays = listOf("Nd", "Pon", "Wt", "Śr", "Czw", "Pt", "Sob")

            (6 downTo 0).map { i ->
                val targetDate = dayStart(now - i * dayMs)
                calendar.timeInMillis = targetDate
                
                val totalVol = allWorkouts
                    .filter { it.isCompleted && it.completionDate != null && dayStart(it.completionDate!!) == targetDate }
                    .sumOf { it.totalScore }

                WeeklyChartPoint(
                    dayName = polishDays[calendar.get(Calendar.DAY_OF_WEEK) - 1],
                    totalVolume = totalVol,
                    timestamp = targetDate
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exerciseProgress = workoutPlans.map { workouts ->
        val completed = workouts.filter { it.isCompleted && it.completionDate != null }
        
        val allPerformedExercises = completed.flatMap { workout ->
            workout.exercises.map { exercise ->
                val bestSetScore = exercise.sets
                    .filter { it.isCompleted }
                    .maxOfOrNull { (it.weightInput.toDoubleOrNull() ?: 0.0) * (it.repsInput.toIntOrNull() ?: 0) } ?: 0.0
                
                ExerciseOccurrence(
                    name = exercise.name,
                    date = workout.completionDate!!,
                    bestScore = bestSetScore
                )
            }
        }

        allPerformedExercises.groupBy { it.name }.mapValues { (_, occurrences) ->
            val now = System.currentTimeMillis()
            
            ExerciseStats(
                today = getBestVolumeInPeriod(occurrences.first().name, workouts, dayStart(now), now),
                lastWeek = getBestVolumeInPeriod(occurrences.first().name, workouts, dayStart(now - 7 * 24 * 60 * 60 * 1000L), dayStart(now)),
                lastMonth = getBestVolumeInPeriod(occurrences.first().name, workouts, dayStart(now - 30 * 24 * 60 * 60 * 1000L), dayStart(now - 7 * 24 * 60 * 60 * 1000L)),
                historyForChart = occurrences.sortedBy { it.date }.map { it.date to it.bestScore }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val todaysProgress = workoutPlans.map { workouts ->
        val todayStart = dayStart(System.currentTimeMillis())
        val todaysWorkouts = workouts.filter { it.isCompleted && it.completionDate!! >= todayStart }

        todaysWorkouts.flatMap { workout ->
            workout.exercises.map { exercise ->
                val completedSets = exercise.sets.filter { it.isCompleted }
                
                val volumes = completedSets.map { 
                    (it.weightInput.toDoubleOrNull() ?: 0.0) * (it.repsInput.toIntOrNull() ?: 0) 
                }

                TodayExerciseStats(
                    name = exercise.name,
                    bestResult = volumes.maxOrNull() ?: 0.0,
                    averageResult = if (volumes.isNotEmpty()) volumes.average() else 0.0
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _summaryData = MutableStateFlow<List<ExerciseComparison>?>(null)
    val summaryData = _summaryData.asStateFlow()

    private val scoringStrategy: ScoringStrategy = VolumeStrategy()

    fun addWorkoutPlan(name: String, days: Set<DayOfWeek>, exercises: List<Exercise>, existingId: String? = null) {
        val workout = Workout(
            id = existingId ?: UUID.randomUUID().toString(),
            name = name,
            dayDescription = days.joinToString(", ") { it.polishName },
            scheduledDays = days,
            exercises = exercises.mapIndexed { exerciseIndex, exercise ->
                val seriesCount = exercise.defaultSeries.toIntOrNull() ?: 0
                val isFirstExercise = exerciseIndex == 0
                
                val sets = mutableListOf<ExerciseSet>()
                
                if (isFirstExercise) {
                    sets.add(ExerciseSet(setNumber = 1, type = SetType.WARMUP, targetReps = "15", targetRir = "0"))
                    sets.add(ExerciseSet(setNumber = 2, type = SetType.WARMUP, targetReps = "10", targetRir = "0"))
                }

                val offset = if (isFirstExercise) 2 else 0
                for (i in 1..seriesCount) {
                    sets.add(
                        ExerciseSet(
                            setNumber = i + offset,
                            targetReps = exercise.defaultReps,
                            targetRir = exercise.defaultRir
                        )
                    )
                }

                exercise.copy(
                    sets = sets,
                    isCompleted = false
                )
            },
            isCompleted = false,
            completionDate = null
        )
        viewModelScope.launch {
            dao.insertWorkout(workout)
        }
    }

    fun removeWorkoutPlan(workout: Workout) {
        viewModelScope.launch {
            dao.deleteWorkout(workout)
        }
    }

    fun updateSetInput(
        workout: Workout,
        exercise: Exercise,
        set: ExerciseSet,
        weight: String? = null,
        reps: String? = null,
        time: String? = null
    ) {
        val updatedExercises = workout.exercises.map { e ->
            if (e.id == exercise.id) {
                e.copy(sets = e.sets.map { s ->
                    if (s.id == set.id) {
                        s.copy(
                            weightInput = weight ?: s.weightInput,
                            repsInput = reps ?: s.repsInput,
                            timeInput = time ?: s.timeInput
                        )
                    } else s
                })
            } else e
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            dao.insertWorkout(updatedWorkout)
        }
    }

    fun toggleExerciseCompletion(workout: Workout, exercise: Exercise) {
        val updatedExercises = workout.exercises.map { e ->
            if (e.id == exercise.id) {
                e.copy(isCompleted = !e.isCompleted)
            } else e
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            dao.insertWorkout(updatedWorkout)
        }
    }

    fun validateExercise(exercise: Exercise): String? {
        exercise.sets.forEach { set ->
            if (exercise.type == ExerciseType.TIME) {
                if (set.timeInput.isBlank()) return "Uzupełnij czas we wszystkich seriach!"
            } else {
                if (set.weightInput.isBlank() || set.repsInput.isBlank()) {
                    return "Uzupełnij ciężar i powtórzenia we wszystkich seriach!"
                }
            }
        }
        return null
    }

    fun finishWorkout(workout: Workout) {
        if (workout.exercises.all { it.isCompleted } && !workout.isCompleted) {
            val finishedWorkout = workout.copy(
                isCompleted = true,
                totalScore = scoringStrategy.calculateScore(workout),
                completionDate = System.currentTimeMillis()
            )

            viewModelScope.launch {
                dao.insertWorkout(finishedWorkout)
            }
        }
    }

    fun finishAndGenerateSummary(workout: Workout) {
        viewModelScope.launch {
            val finishedWorkout = workout.copy(
                isCompleted = true,
                totalScore = scoringStrategy.calculateScore(workout),
                completionDate = System.currentTimeMillis()
            )
            dao.insertWorkout(finishedWorkout)

            val allWorkouts = dao.getAllPlans().first() 
            val now = System.currentTimeMillis()

            val summary = workout.exercises.map { exercise ->
                ExerciseComparison(
                    exerciseName = exercise.name,
                    todayMax = getBestVolumeInPeriod(exercise.name, allWorkouts, dayStart(now), now),
                    weekAgoMax = getBestVolumeInPeriod(exercise.name, allWorkouts, dayStart(now - 7 * 24 * 60 * 60 * 1000L), dayStart(now - 6 * 24 * 60 * 60 * 1000L)),
                    monthAgoMax = getBestVolumeInPeriod(exercise.name, allWorkouts, dayStart(now - 31 * 24 * 60 * 60 * 1000L), dayStart(now - 27 * 24 * 60 * 60 * 1000L))
                )
            }

            _summaryData.value = summary
        }
    }

    fun clearSummary() { _summaryData.value = null }

    fun getWorkoutForToday(currentDay: DayOfWeek): Flow<Workout?> {
        return workoutPlans.map { workouts ->
            val workout = workouts.find { it.scheduledDays.contains(currentDay) }
            if (workout != null && workout.isCompleted && !isToday(workout.completionDate)) {
                workout.copy(
                    isCompleted = false,
                    completionDate = null,
                    exercises = workout.exercises.map { e ->
                        e.copy(
                            isCompleted = false,
                            sets = e.sets.map { s ->
                                s.copy(weightInput = "", repsInput = "", timeInput = "", isCompleted = false)
                            }
                        )
                    }
                )
            } else {
                workout
            }
        }
    }

    private fun isToday(date: Long?): Boolean {
        if (date == null) return false
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = date }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }


    private fun getBestVolumeInPeriod(exerciseName: String, allWorkouts: List<Workout>, start: Long, end: Long): Double {
        return allWorkouts
            .filter { it.isCompleted && it.completionDate != null && it.completionDate!! in start..end }
            .flatMap { it.exercises }
            .filter { it.name == exerciseName }
            .flatMap { it.sets }
            // Poprawka: bierzemy pod uwagę serię, jeśli ma wpisane dane (nawet bez isCompleted)
            .filter { it.weightInput.isNotBlank() && it.repsInput.isNotBlank() }
            .maxOfOrNull {
                val weightCleaned = it.weightInput.replace(',', '.')
                val w = weightCleaned.toDoubleOrNull() ?: 0.0
                val r = it.repsInput.toIntOrNull() ?: 0
                w * r
            } ?: 0.0
    }

    /*private fun getBestVolumeInPeriod(exerciseName: String, allWorkouts: List<Workout>, start: Long, end: Long): Double {
        return allWorkouts
            .filter { it.isCompleted && it.completionDate != null && it.completionDate!! in start..end }
            .flatMap { it.exercises }
            .filter { it.name == exerciseName }
            .flatMap { it.sets }
            .filter { it.isCompleted }
            .maxOfOrNull { (it.weightInput.toDoubleOrNull() ?: 0.0) * (it.repsInput.toIntOrNull() ?: 0) } ?: 0.0
    }*/

    private fun dayStart(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun calculateStreak(workouts: List<Workout>): Int {
        val completedDatesSet = workouts
            .filter { it.isCompleted && it.completionDate != null }
            .map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.completionDate!! }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }
            .toSet()

        val allScheduledDays = workouts.flatMap { it.scheduledDays }.toSet()
        if (allScheduledDays.isEmpty() && completedDatesSet.isEmpty()) return 0

        var streak = 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        for (i in 0 until 365) {
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
            val dayOfWeek = calendarDayToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))
            val isScheduled = allScheduledDays.contains(dayOfWeek)
            val isCompleted = completedDatesSet.contains(dateKey)

            if (isCompleted) {
                streak++
            } else if (isScheduled) {
                if (i > 0) {
                    break 
                }
            } else {
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return streak
    }

    private fun calendarDayToDayOfWeek(calendarDay: Int): DayOfWeek {
        return when (calendarDay) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.SUNDAY
        }
    }
}

data class ExerciseOccurrence(val name: String, val date: Long, val bestScore: Double)

data class ExerciseStats(
    val today: Double,
    val lastWeek: Double,
    val lastMonth: Double,
    val historyForChart: List<Pair<Long, Double>>
)

data class TodayExerciseStats(
    val name: String,
    val bestResult: Double,
    val averageResult: Double
)
