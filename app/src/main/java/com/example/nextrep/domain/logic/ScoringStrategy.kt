package com.example.nextrep.domain.logic

import com.example.nextrep.domain.models.Workout

/*
  WZORZEC PROJEKTOWY: STRATEGIA (Strategy Pattern)
  Definiuje wspólny interfejs dla różnych algorytmów obliczania wyniku treningu.
  Pozwala na dynamiczną zmianę logiki punktacji bez modyfikacji klasy WorkoutViewModel.
 */
interface ScoringStrategy {
    fun calculateScore(workout: Workout): Double
}

// Konkretna implementacja strategii: Obliczanie na podstawie sumarycznej objętości (kg * powtórzenia)
class VolumeStrategy : ScoringStrategy {
    override fun calculateScore(workout: Workout): Double {
        var totalVolume = 0.0
        workout.exercises.forEach { exercise ->
            exercise.sets.forEach { set ->
                val weight = set.weightInput.toDoubleOrNull() ?: 0.0
                val reps = set.repsInput.toIntOrNull() ?: 0

                totalVolume += (weight * reps)
            }
        }
        return totalVolume
    }
}

// Alternatywna implementacja strategii (przykładowa)
class FeelingStrategy : ScoringStrategy {
    override fun calculateScore(workout: Workout): Double {
        var totalScore = 0.0
        workout.exercises.forEach { exercise ->
            exercise.sets.forEach { set ->
                val weight = set.weightInput.toDoubleOrNull() ?: 0.0
                val reps = set.repsInput.toIntOrNull() ?: 0

                if (weight > 0 && reps > 0) {
                    totalScore += (weight * reps * 1.2)
                }
            }
        }
        return totalScore
    }
}
