package com.example.nextrep.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items 
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextrep.domain.models.*
import com.example.nextrep.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(viewModel: WorkoutViewModel) {
    var workoutName by remember { mutableStateOf("") }
    var editingWorkoutId by remember { mutableStateOf<String?>(null) }
    val selectedDays = remember { mutableStateListOf<DayOfWeek>() }
    val exercises = remember { mutableStateListOf<Exercise>() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text(
                text = if (editingWorkoutId == null) "Nowy Plan Treningowy" else "Edytujesz Plan",
                fontSize = 24.sp, 
                color = Color.White, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Nazwa (np. Trening A, FBW 1)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Dni, w które wykonujesz ten trening:", color = GreyText, fontSize = 14.sp)

            FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                DayOfWeek.entries.forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (isSelected) selectedDays.remove(day) else selectedDays.add(day) },
                        label = { Text(day.polishName.take(3)) },
                        modifier = Modifier.padding(4.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GorillaGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        itemsIndexed(exercises) { index, exercise ->
            ExerciseEditCard(
                exercise = exercise,
                onDelete = { exercises.removeAt(index) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Button(
                onClick = { exercises.add(Exercise(name = "")) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("+ Dodaj Ćwiczenie")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (workoutName.isNotBlank() && exercises.isNotEmpty() && selectedDays.isNotEmpty()) {
                        viewModel.addWorkoutPlan(
                            name = workoutName, 
                            days = selectedDays.toSet(), 
                            exercises = exercises.toList(),
                            existingId = editingWorkoutId
                        )
                        workoutName = ""
                        selectedDays.clear()
                        exercises.clear()
                        editingWorkoutId = null
                        android.widget.Toast.makeText(context, "Zapisano zmiany!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Uzupełnij nazwę, dni i ćwiczenia!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GorillaGreen)
            ) {
                Text(
                    text = if (editingWorkoutId == null) "ZAPISZ CAŁY PLAN 🦍" else "ZAPISZ ZMIANY 🔥", 
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (editingWorkoutId != null) {
                TextButton(
                    onClick = {
                        workoutName = ""
                        selectedDays.clear()
                        exercises.clear()
                        editingWorkoutId = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ANULUJ EDYCJĘ", color = Color.Gray)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
            Text("Twoje aktualne plany:", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(viewModel.workoutPlans.value) { plan ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = GymCard.copy(alpha = 0.6f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plan.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(plan.dayDescription, color = GorillaGreen, fontSize = 12.sp)
                    }
                    TextButton(onClick = {
                        workoutName = plan.name
                        editingWorkoutId = plan.id
                        selectedDays.clear()
                        selectedDays.addAll(plan.scheduledDays)
                        exercises.clear()
                        // Tworzymy kopie ćwiczeń, żeby nie edytować oryginałów bezpośrednio przed zapisem
                        exercises.addAll(plan.exercises.map { it.copy() })
                    }) {
                        Text("EDYTUJ", color = Color.Yellow)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseEditCard(exercise: Exercise, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(exercise.name) }
    var type by remember { mutableStateOf(exercise.type) }
    var series by remember { mutableStateOf(exercise.defaultSeries) }
    var reps by remember { mutableStateOf(exercise.defaultReps) }
    var rir by remember { mutableStateOf(exercise.defaultRir) }

    // Synchronizacja stanu w górę
    LaunchedEffect(name, type, series, reps, rir) {
        exercise.name = name
        exercise.type = type
        exercise.defaultSeries = series
        exercise.defaultReps = reps
        exercise.defaultRir = rir
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymCard),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nazwa ćwiczenia") },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GorillaGreen)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                FilterChip(
                    selected = type == ExerciseType.REPS_AND_WEIGHT,
                    onClick = { type = ExerciseType.REPS_AND_WEIGHT },
                    label = { Text("Siłowe", fontSize = 12.sp) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                FilterChip(
                    selected = type == ExerciseType.TIME,
                    onClick = { type = ExerciseType.TIME },
                    label = { Text("Wytrzymałościowe", fontSize = 12.sp) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (type == ExerciseType.REPS_AND_WEIGHT) {
                    EditParamField("Serie", series, Modifier.weight(1f)) { series = it }
                    EditParamField("Powt.", reps, Modifier.weight(1f)) { reps = it }
                    EditParamField("RIR", rir, Modifier.weight(1f)) { rir = it }
                } else {
                    EditParamField("Serie", series, Modifier.weight(1f)) { series = it }
                    EditParamField("Czas (sek)", reps, Modifier.weight(2f)) { reps = it }
                    EditParamField("RIR", rir, Modifier.weight(1f)) { rir = it }
                }
            }
        }
    }
}


@Composable
fun EditParamField(label: String, value: String, modifier: Modifier, onValueChange: (String) -> Unit) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = GreyText)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(8.dp)
        )
    }
}