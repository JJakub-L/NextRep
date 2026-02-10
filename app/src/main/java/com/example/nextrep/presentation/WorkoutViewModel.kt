package com.example.nextrep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextrep.data.local.WorkoutDao
import com.example.nextrep.domain.logic.ScoringStrategy
import com.example.nextrep.domain.logic.VolumeStrategy
import com.example.nextrep.domain.models.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class WorkoutViewModel(private val dao: WorkoutDao) : ViewModel() {

    // 1. Obserwujemy plany z bazy
    val workoutPlans: StateFlow<List<Workout>> = dao.getAllPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. NOWOŚĆ: Streak liczony automatycznie z bazy danych
    // Za każdym razem, gdy zmieni się lista workoutPlans, streak przeliczy się sam.
    val streak: StateFlow<Int> = workoutPlans
        .map { workouts -> calculateStreak(workouts) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val scoringStrategy: ScoringStrategy = VolumeStrategy()

    // ... (Twoje metody addWorkoutPlan, updateSetInput, removeWorkoutPlan zostają bez zmian)

    // 3. POPRAWKA: Metoda finishWorkout musi zapisywać datę ukończenia
    fun finishWorkout(workout: Workout) {
        if (workout.exercises.all { it.isCompleted } && !workout.isCompleted) {
            val finishedWorkout = workout.copy(
                isCompleted = true,
                totalScore = scoringStrategy.calculateScore(workout),
                completionDate = System.currentTimeMillis() // To jest kluczowe dla streaku!
            )

            viewModelScope.launch {
                dao.insertWorkout(finishedWorkout)
            }
        }
    }

    // 4. LOGIKA OBLICZANIA STREAKU
    private fun calculateStreak(workouts: List<Workout>): Int {
        val completedDates = workouts
            .filter { it.isCompleted && it.completionDate != null }
            .map { it.completionDate!! }
            .distinctBy {
                // Grupowanie po dniach (ignorujemy godziny/minuty)
                val cal = Calendar.getInstance().apply { timeInMillis = it }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }
            .sortedDescending()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        val calendar = Calendar.getInstance()

        // Dzisiejsza data (początek dnia)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        var currentCheckDate = calendar.timeInMillis

        for (date in completedDates) {
            val diff = currentCheckDate - date
            val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)

            if (daysDiff <= 0) {
                // Trening z dzisiaj
                streak++
            } else if (daysDiff <= 1) {
                // Trening z wczoraj (zachowujemy ciągłość)
                streak++
                currentCheckDate = date
            } else {
                // Przerwa dłuższa niż 1 dzień - koniec serii
                break
            }
        }
        return streak
    }
}