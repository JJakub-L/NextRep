package com.example.nextrep.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextrep.domain.models.Exercise
import com.example.nextrep.domain.models.ExerciseSet
import com.example.nextrep.domain.models.SetType
import com.example.nextrep.domain.models.Workout
import com.example.nextrep.ui.theme.*

@Composable
fun WorkoutScreenContent(viewModel: WorkoutViewModel) {
    val workout = viewModel.workouts.value.firstOrNull()
    val context = LocalContext.current

    if (workout == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Brak planu treningowego", color = Color.White)
        }
    } else {
        // ZMIANA: Czytamy .value - teraz ekran wie natychmiast, że skończyłeś!
        if (workout.isCompleted.value) {
            SuccessScreen(workoutName = workout.name)
        } else {
            ActiveWorkoutScreen(workout, viewModel, context)
        }
    }
}

@Composable
fun ActiveWorkoutScreen(workout: Workout, viewModel: WorkoutViewModel, context: android.content.Context) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // <--- FIX: To sprawia, że lista ucieka przed klawiaturą!
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = workout.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(
            items = workout.exercises,
            key = { it.id }
        ) { exercise ->
            ExerciseCard(exercise, viewModel, context)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Czytamy .value
            val allDone = workout.exercises.all { it.isCompleted.value }

            Button(
                enabled = allDone,
                onClick = { viewModel.finishWorkout(workout) },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allDone) GorillaGreen else Color.DarkGray,
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (allDone) "ZAKOŃCZ TRENING 🏁" else "ZATWIERDŹ WSZYSTKIE ĆWICZENIA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (allDone) Color.White else Color.Gray
                )
            }
        }
    }
}

// Reszta kodu (ExerciseCard, SetRow, SmallInput, SuccessScreen) zostaje TAKA SAMA jak ostatnio.
// Skopiuj ExerciseCard z poprzedniej odpowiedzi, jeśli go tu nie widzisz.
// Wklejam je poniżej dla 100% pewności, że nic nie zginie:

@Composable
fun ExerciseCard(exercise: Exercise, viewModel: WorkoutViewModel, context: android.content.Context) {
    val isDone = exercise.isCompleted.value

    val cardBg = if (isDone) Color(0xFF1B301B) else GymCard
    val border = if (isDone) BorderStroke(2.dp, GorillaGreen) else null

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = border,
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exercise.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) GorillaGreen else Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GorillaGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Seria", modifier = Modifier.weight(1.2f), color = GreyText, fontSize = 12.sp)
                Text("Cel", modifier = Modifier.weight(1.5f), color = GreyText, fontSize = 12.sp)
                Text("kg", modifier = Modifier.weight(1f), color = GreyText, fontSize = 12.sp)
                Text("Powt.", modifier = Modifier.weight(1f), color = GreyText, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            exercise.sets.forEach { set ->
                key(set.id) {
                    SetRow(set, isDone)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isDone) {
                        viewModel.toggleExerciseCompletion(exercise)
                    } else {
                        val error = viewModel.validateExercise(exercise)
                        if (error == null) {
                            viewModel.toggleExerciseCompletion(exercise)
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDone) Color.Transparent else GorillaGreen
                ),
                border = if (isDone) BorderStroke(1.dp, GorillaGreen) else null,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isDone) "EDYTUJ" else "ZAKOŃCZ ĆWICZENIE",
                    color = if (isDone) GorillaGreen else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SetRow(set: ExerciseSet, isReadOnly: Boolean) {
    val rowColor = if (set.type == SetType.WARMUP) Color(0xFF2C2C2C) else Color.Transparent
    val labelColor = if (set.type == SetType.WARMUP) Color(0xFFE0E0E0) else BrightGreen

    val weightState = set.weightInput
    val repsState = set.repsInput

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor, shape = RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val label = if (set.type == SetType.WARMUP) "Pre-${set.setNumber}" else "${set.setNumber}"
        Text(label, color = labelColor, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))

        Column(modifier = Modifier.weight(1.5f)) {
            Text("${set.targetReps} powt.", color = Color.White, fontSize = 12.sp)
            if (set.type == SetType.WORKING) Text("RIR: ${set.targetRir}", color = GreyText, fontSize = 10.sp)
        }

        SmallInput(weightState, isReadOnly, Modifier.weight(1f).padding(end = 4.dp))
        SmallInput(repsState, isReadOnly, Modifier.weight(1f))
    }
}

@Composable
fun SmallInput(state: MutableState<String>, isReadOnly: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(35.dp).background(Color(0xFF121212), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = state.value,
            onValueChange = { if (!isReadOnly) state.value = it },
            enabled = !isReadOnly,
            textStyle = TextStyle(
                color = if (isReadOnly) Color.Gray else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(GorillaGreen),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SuccessScreen(workoutName: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(GorillaGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ThumbUp, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("GRATULACJE!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = GorillaGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Trening \"$workoutName\"\nzakończony sukcesem.", fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Text("JESTEŚ SUPER! 🦍🔥", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Yellow)
        Spacer(modifier = Modifier.height(48.dp))
        Text("Odpocznij i zjedz coś dobrego.", fontSize = 14.sp, color = GreyText)
    }
}