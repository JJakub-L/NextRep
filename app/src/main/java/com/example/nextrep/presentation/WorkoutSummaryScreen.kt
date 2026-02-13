package com.example.nextrep.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextrep.domain.models.ExerciseComparison

@Composable
fun WorkoutSummaryScreen(comparisonList: List<ExerciseComparison>, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Ciemne tło
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "PODSUMOWANIE TRENINGU",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(comparisonList) { item ->
                SummaryCard(item)
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF557D45)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ZAMKNIJ", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryCard(data: ExerciseComparison) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9))
    ) {
        Column {
            // Nagłówek ćwiczenia (Zielony pas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF557D45))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(data.exerciseName, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Tabela wyników
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ResultItem("Miesiąc temu", data.monthAgoMax)
                ResultItem("Tydzień temu", data.weekAgoMax)
                ResultItem("Dziś", data.todayMax)
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.DarkGray)
        Text(
            text = if (value > 0.0) "${value.toInt()} kg" else "---",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
    }
}
