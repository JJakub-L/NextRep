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

    private val scoringStrategy: ScoringStrategy = VolumeStrategy()

    fun addWorkoutPlan(name: String, days: Set<DayOfWeek>, exercises: List<Exercise>, existingId: String? = null) {
        val workout = Workout(
            id = existingId ?: UUID.randomUUID().toString(),
            name = name,
            dayDescription = days.joinToString(", ") { it.polishName },
            scheduledDays = days,
            exercises = exercises.map { exercise ->
                val seriesCount = exercise.defaultSeries.toIntOrNull() ?: 0
                // Zawsze tworzymy świeże zestawy dla planu, aby nie przenosić wyników z poprzednich treningów
                val sets = (1..seriesCount).map { i ->
                    ExerciseSet(
                        setNumber = i,
                        targetReps = exercise.defaultReps,
                        targetRir = exercise.defaultRir
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

    // Funkcja pomocnicza do pobierania planu na dziś z automatycznym resetem jeśli ukończony w innym dniu
    fun getWorkoutForToday(currentDay: DayOfWeek): Flow<Workout?> {
        return workoutPlans.map { workouts ->
            val workout = workouts.find { it.scheduledDays.contains(currentDay) }
            if (workout != null && workout.isCompleted && !isToday(workout.completionDate)) {
                // Resetujemy stan treningu na nowy dzień
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

    private fun calculateStreak(workouts: List<Workout>): Int {
        val completedDates = workouts
            .filter { it.isCompleted && it.completionDate != null }
            .map { it.completionDate!! }
            .distinctBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }
            .sortedDescending()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var currentCheckDate = calendar.timeInMillis

        for (date in completedDates) {
            val diff = currentCheckDate - date
            val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)

            if (daysDiff <= 0) {
                streak++
            } else if (daysDiff <= 1) {
                streak++
                currentCheckDate = date
            } else {
                break
            }
        }
        return streak
    }
}
