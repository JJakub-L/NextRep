package com.example.nextrep.domain.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

enum class SetType {
    WARMUP, WORKING
}

data class ExerciseSet(
    val id: String = UUID.randomUUID().toString(),
    val setNumber: Int,
    val type: SetType = SetType.WORKING,
    val targetReps: String,
    val targetRir: String,
    var weightInput: MutableState<String> = mutableStateOf(""),
    var repsInput: MutableState<String> = mutableStateOf(""),
    var isCompleted: MutableState<Boolean> = mutableStateOf(false)
)

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sets: MutableList<ExerciseSet> = mutableListOf(),
    var isCompleted: MutableState<Boolean> = mutableStateOf(false)
)

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dayDescription: String,
    val exercises: MutableList<Exercise> = mutableListOf(),

    // ZMIANA: Teraz Trening też jest "żywy" i powiadomi ekran o zmianie!
    var isCompleted: MutableState<Boolean> = mutableStateOf(false),
    var totalScore: MutableState<Double> = mutableStateOf(0.0)
)