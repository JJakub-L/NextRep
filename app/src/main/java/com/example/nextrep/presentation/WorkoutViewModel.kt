package com.example.nextrep.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nextrep.domain.WorkoutRepository
import com.example.nextrep.domain.logic.ScoringStrategy
import com.example.nextrep.domain.logic.VolumeStrategy
import com.example.nextrep.domain.models.Exercise
import com.example.nextrep.domain.models.Workout

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: State<List<Workout>> = _workouts

    private val _streak = mutableStateOf(12)
    val streak: State<Int> = _streak

    private val scoringStrategy: ScoringStrategy = VolumeStrategy()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        _workouts.value = repository.getAllWorkouts()
    }

    fun toggleExerciseCompletion(exercise: Exercise) {
        exercise.isCompleted.value = !exercise.isCompleted.value
    }

    fun validateExercise(exercise: Exercise): String? {
        exercise.sets.forEach { set ->
            if (set.weightInput.value.isBlank() || set.repsInput.value.isBlank()) {
                return "Uzupełnij wszystkie serie w ćwiczeniu: ${exercise.name}"
            }
        }
        return null
    }

    fun finishWorkout(workout: Workout) {
        val allExercisesDone = workout.exercises.all { it.isCompleted.value }

        // ZMIANA: Używamy .value
        if (allExercisesDone && !workout.isCompleted.value) {
            workout.isCompleted.value = true // <--- Tu jest klucz do sukcesu!
            workout.totalScore.value = scoringStrategy.calculateScore(workout)
            _streak.value += 1
            repository.saveWorkout(workout)
            // Wymuszamy odświeżenie listy (dla pewności, choć MutableState powinno załatwić sprawę)
            _workouts.value = repository.getAllWorkouts()
        }
    }
}