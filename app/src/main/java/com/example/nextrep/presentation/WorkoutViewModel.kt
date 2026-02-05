package com.example.nextrep.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nextrep.domain.WorkoutRepository
import com.example.nextrep.domain.logic.ScoringStrategy
import com.example.nextrep.domain.logic.VolumeStrategy
import com.example.nextrep.domain.models.* // Importuje DayOfWeek, Exercise itp.

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: State<List<Workout>> = _workouts

    // Lista planów treningowych (szablonów)
    private val _workoutPlans = mutableStateOf<List<Workout>>(emptyList())
    val workoutPlans: State<List<Workout>> = _workoutPlans

    private val _streak = mutableStateOf(12)
    val streak: State<Int> = _streak

    private val scoringStrategy: ScoringStrategy = VolumeStrategy()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        _workouts.value = repository.getAllWorkouts()
    }

    // Ulepszona funkcja dodawania/edycji planu
    fun addWorkoutPlan(name: String, days: Set<DayOfWeek>, exercises: List<Exercise>, existingId: String? = null) {
        // Przygotowujemy serie dla każdego ćwiczenia
        exercises.forEach { ex ->
            val count = ex.defaultSeries.toIntOrNull() ?: 1
            val setsList = mutableListOf<ExerciseSet>()
            for (i in 1..count) {
                setsList.add(ExerciseSet(
                    setNumber = i,
                    targetReps = ex.defaultReps,
                    targetRir = ex.defaultRir
                ))
            }
            ex.sets.clear()
            ex.sets.addAll(setsList)
        }

        val updatedPlans = _workoutPlans.value.toMutableList()
        val existingIndex = existingId?.let { id -> updatedPlans.indexOfFirst { it.id == id } } ?: -1

        val workout = if (existingIndex != -1) {
            // Edycja istniejącego
            updatedPlans[existingIndex].copy(
                name = name,
                dayDescription = days.joinToString(", ") { it.polishName },
                scheduledDays = days,
                exercises = exercises.toMutableList()
            )
        } else {
            // Nowy plan
            Workout(
                name = name,
                dayDescription = days.joinToString(", ") { it.polishName },
                scheduledDays = days,
                exercises = exercises.toMutableList()
            )
        }

        if (existingIndex != -1) {
            updatedPlans[existingIndex] = workout
        } else {
            updatedPlans.add(workout)
        }

        _workoutPlans.value = updatedPlans
        _workouts.value = updatedPlans // Synchronizacja z widokiem treningów
        
        // Tutaj można dodać zapis do repozytorium jeśli potrzebne
    }
    
    fun removeWorkoutPlan(workout: Workout) {
        _workoutPlans.value = _workoutPlans.value.filter { it.id != workout.id }
        _workouts.value = _workoutPlans.value
    }

    fun toggleExerciseCompletion(exercise: Exercise) {
        exercise.isCompleted.value = !exercise.isCompleted.value
    }

    fun validateExercise(exercise: Exercise): String? {
        exercise.sets.forEach { set ->
            if (exercise.type == ExerciseType.TIME) {
                if (set.timeInput.value.isBlank()) return "Uzupełnij czas dla: ${exercise.name}"
            } else {
                if (set.weightInput.value.isBlank() || set.repsInput.value.isBlank()) {
                    return "Uzupełnij wszystkie serie w: ${exercise.name}"
                }
            }
        }
        return null
    }

    fun finishWorkout(workout: Workout) {
        if (workout.exercises.all { it.isCompleted.value } && !workout.isCompleted.value) {
            workout.isCompleted.value = true
            workout.totalScore.value = scoringStrategy.calculateScore(workout)
            _streak.value += 1
            repository.saveWorkout(workout)
        }
    }
}