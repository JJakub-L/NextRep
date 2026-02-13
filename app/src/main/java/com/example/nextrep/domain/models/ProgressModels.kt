package com.example.nextrep.domain.models

// 1. Wynik konkretnego ćwiczenia w tabeli
data class ExerciseComparison(
    val exerciseName: String,
    val todayMax: Double,
    val weekAgoMax: Double,
    val monthAgoMax: Double
)

// 2. Cała karta planu treningowego (np. "Trening A")
data class TrainingProgressCard(
    val trainingName: String,
    val exercises: List<ExerciseComparison>
)

// 3. Punkt na wykresie tygodniowym (Suma wszystkich treningów danego dnia)
data class WeeklyChartPoint(
    val dayName: String, // np. "Pon", "Wt"
    val totalVolume: Double,
    val timestamp: Long
)