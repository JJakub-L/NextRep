package com.example.nextrep.domain.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

enum class SetType { WARMUP, WORKING }

// Typ ćwiczenia: Siłowe vs Na czas (Plank)
enum class ExerciseType { REPS_AND_WEIGHT, TIME }

// Dni tygodnia
enum class DayOfWeek(val polishName: String) {
    MONDAY("Poniedziałek"), TUESDAY("Wtorek"), WEDNESDAY("Środa"),
    THURSDAY("Czwartek"), FRIDAY("Piątek"), SATURDAY("Sobota"), SUNDAY("Niedziela")
}

data class ExerciseSet(
    val id: String = UUID.randomUUID().toString(),
    val setNumber: Int,
    val type: SetType = SetType.WORKING,
    val targetReps: String,
    val targetRir: String,
    var weightInput: MutableState<String> = mutableStateOf(""),
    var repsInput: MutableState<String> = mutableStateOf(""),
    var timeInput: MutableState<String> = mutableStateOf(""), // Dla planków
    var isCompleted: MutableState<Boolean> = mutableStateOf(false)
)

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var type: ExerciseType = ExerciseType.REPS_AND_WEIGHT,
    val sets: MutableList<ExerciseSet> = mutableListOf(),
    var isCompleted: MutableState<Boolean> = mutableStateOf(false),

    // NOWE POLA DO KREATORA:
    var defaultSeries: String = "3",
    var defaultReps: String = "10",
    var defaultRir: String = "2",
    var defaultTempo: String = "2110",
    var defaultRest: String = "2 min"
)

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dayDescription: String,
    val scheduledDays: Set<DayOfWeek> = emptySet(),
    val exercises: MutableList<Exercise> = mutableListOf(),
    var isCompleted: MutableState<Boolean> = mutableStateOf(false),
    var totalScore: MutableState<Double> = mutableStateOf(0.0)
)