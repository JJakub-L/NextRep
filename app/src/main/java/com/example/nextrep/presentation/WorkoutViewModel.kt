package com.example.nextrep.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextrep.data.local.WorkoutDao
import com.example.nextrep.domain.logic.ScoringStrategy
import com.example.nextrep.domain.logic.VolumeStrategy
import com.example.nextrep.domain.models.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
            // Używamy mapIndexed dla pewności indeksowania
            val exercisesToSave = exercises.mapIndexed { index, originalEx ->
                val setsList = mutableListOf<ExerciseSet>()
                
                // Zawsze dodajemy 3 preserie dla PIERWSZEGO ćwiczenia w całym planie
                if (index == 0) {
                    for (i in 1..3) {
                        setsList.add(ExerciseSet(
                            setNumber = i,
                            type = SetType.WARMUP,
                            targetReps = "5",
                            targetRir = "-"
                        ))
                    }
                }

                // Dodajemy serie robocze na podstawie domyślnych wartości z kreatora
                val workingSetsCount = originalEx.defaultSeries.toIntOrNull() ?: 1
                val offset = if (index == 0) 3 else 0 // Przesuwamy numerację jeśli są preserie
                for (i in 1..workingSetsCount) {
                    setsList.add(ExerciseSet(
                        setNumber = i + offset,
                        type = SetType.WORKING,
                        targetReps = originalEx.defaultReps,
                        targetRir = originalEx.defaultRir
                    ))
                }
                
                // Tworzymy kopię ćwiczenia z nową listą serii
                originalEx.copy(sets = setsList)
            }

            val workout = Workout(
                id = existingId ?: java.util.UUID.randomUUID().toString(),
                name = name,
                dayDescription = days.joinToString(", ") { it.polishName },
                scheduledDays = days,
                exercises = exercisesToSave.toMutableList()
            )

            dao.insertWorkout(workout)
        }
    }

    fun removeWorkoutPlan(workout: Workout) {
        viewModelScope.launch {
            dao.deleteWorkout(workout)
        }
    }

    fun toggleExerciseCompletion(exercise: Exercise) {
        exercise.isCompleted = !exercise.isCompleted
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
            workout.isCompleted = true
            workout.totalScore = scoringStrategy.calculateScore(workout)
            _streak.value += 1
            
            viewModelScope.launch {
                dao.insertWorkout(workout)
            }
        }
    }
}
