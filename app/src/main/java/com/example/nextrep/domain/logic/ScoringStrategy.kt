package com.example.nextrep.domain.logic

import com.example.nextrep.domain.models.Workout

interface ScoringStrategy {
    fun calculateScore(workout: Workout): Double
}

class VolumeStrategy : ScoringStrategy {
    override fun calculateScore(workout: Workout): Double {
        var totalVolume = 0.0
        workout.exercises.forEach { exercise ->
            exercise.sets.forEach { set ->
                // ZMIANA: Dodaliśmy .value
                val weight = set.weightInput.value.toDoubleOrNull() ?: 0.0
                val reps = set.repsInput.value.toIntOrNull() ?: 0

                totalVolume += (weight * reps)
            }
        }
        return totalVolume
    }
}

class FeelingStrategy : ScoringStrategy {
    override fun calculateScore(workout: Workout): Double {
        var totalScore = 0.0
        workout.exercises.forEach { exercise ->
            exercise.sets.forEach { set ->
                // ZMIANA: Dodaliśmy .value
                val weight = set.weightInput.value.toDoubleOrNull() ?: 0.0
                val reps = set.repsInput.value.toIntOrNull() ?: 0

                if (weight > 0 && reps > 0) {
                    totalScore += (weight * reps * 1.2)
                }
            }
        }
        return totalScore
    }
}