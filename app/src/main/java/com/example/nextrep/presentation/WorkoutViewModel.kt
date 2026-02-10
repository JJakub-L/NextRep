package com.example.nextrep.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextrep.data.local.WorkoutDao
import com.example.nextrep.domain.logic.ScoringStrategy
import com.example.nextrep.domain.logic.VolumeStrategy
import com.example.nextrep.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(private val dao: WorkoutDao) : ViewModel() {

    val workoutPlans: StateFlow<List<Workout>> = dao.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _streak = mutableStateOf(12)
    val streak = _streak

    private val scoringStrategy: ScoringStrategy = VolumeStrategy()

    fun addWorkoutPlan(name: String, days: Set<DayOfWeek>, exercises: List<Exercise>, existingId: String? = null) {
        viewModelScope.launch {
            val exercisesToSave = exercises.mapIndexed { index, ex ->
                val newSets = mutableListOf<ExerciseSet>()
                
                if (index == 0) {
                    for (i in 1..3) {
                        newSets.add(ExerciseSet(
                            setNumber = i,
                            type = SetType.WARMUP,
                            targetReps = "5",
                            targetRir = "-"
                        ))
                    }
                }

                val workingSetsCount = ex.defaultSeries.toIntOrNull() ?: 1
                val offset = if (index == 0) 3 else 0
                for (i in 1..workingSetsCount) {
                    newSets.add(ExerciseSet(
                        setNumber = i + offset,
                        type = SetType.WORKING,
                        targetReps = ex.defaultReps,
                        targetRir = ex.defaultRir
                    ))
                }
                
                ex.copy(sets = newSets)
            }

            val workout = Workout(
                id = existingId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                dayDescription = days.joinToString(", ") { it.polishName },
                scheduledDays = days,
                exercises = exercisesToSave
            )

            dao.insertWorkout(workout)
        }
    }

    // NOWA METODA: Aktualizacja pola w serii w sposób niemutowalny
    fun updateSetInput(workout: Workout, exercise: Exercise, set: ExerciseSet, weight: String? = null, reps: String? = null, time: String? = null) {
        viewModelScope.launch {
            val updatedExercises = workout.exercises.map { ex ->
                if (ex.id == exercise.id) {
                    val updatedSets = ex.sets.map { s ->
                        if (s.id == set.id) {
                            s.copy(
                                weightInput = weight ?: s.weightInput,
                                repsInput = reps ?: s.repsInput,
                                timeInput = time ?: s.timeInput
                            )
                        } else s
                    }
                    ex.copy(sets = updatedSets)
                } else ex
            }
            val updatedWorkout = workout.copy(exercises = updatedExercises)
            dao.insertWorkout(updatedWorkout)
        }
    }

    fun removeWorkoutPlan(workout: Workout) {
        viewModelScope.launch {
            dao.deleteWorkout(workout)
        }
    }

    fun toggleExerciseCompletion(workout: Workout, exercise: Exercise) {
        viewModelScope.launch {
            val updatedExercises = workout.exercises.map { ex ->
                if (ex.id == exercise.id) {
                    ex.copy(isCompleted = !ex.isCompleted)
                } else ex
            }
            val updatedWorkout = workout.copy(exercises = updatedExercises)
            dao.insertWorkout(updatedWorkout)
        }
    }

    fun validateExercise(exercise: Exercise): String? {
        exercise.sets.forEach { set ->
            if (exercise.type == ExerciseType.TIME) {
                if (set.timeInput.isBlank()) return "Uzupełnij czas dla: ${exercise.name}"
            } else {
                if (set.weightInput.isBlank() || set.repsInput.isBlank()) {
                    return "Uzupełnij wszystkie serie w: ${exercise.name}"
                }
            }
        }
        return null
    }

    fun finishWorkout(workout: Workout) {
        if (workout.exercises.all { it.isCompleted } && !workout.isCompleted) {
            val finishedWorkout = workout.copy(
                isCompleted = true,
                totalScore = scoringStrategy.calculateScore(workout)
            )
            _streak.value += 1
            
            viewModelScope.launch {
                dao.insertWorkout(finishedWorkout)
            }
        }
    }
}
