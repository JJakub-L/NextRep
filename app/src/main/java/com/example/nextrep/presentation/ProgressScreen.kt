package com.example.nextrep.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextrep.domain.models.*

@Composable
fun ProgressScreen(viewModel: WorkoutViewModel) {
    val progressCards by viewModel.progressCards.collectAsState()
    val weeklyData by viewModel.weeklyChartData.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Nagłówek główny
            item {
                Text(
                    "Postępy",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // GÓRA: Karty porównawcze
            items(progressCards) { card ->
                TrainingProgressCard(card)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ŚRODEK: Wykres
            item {
                Text(
                    "Aktywność Tygodniowa",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                WeeklyProgressChart(weeklyData)
            }

            // DÓŁ: Wyjaśnienie
            item {
                CalculationInfoSection()
            }
        }
    }
}

@Composable
fun CalculationInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 40.dp)
            .background(Color(0xFF252525), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF557D45),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Jak obliczamy Twoje wyniki?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        MethodologyItem(
            title = "Najlepszy wynik (PR)",
            description = "To najwyższa objętość uzyskana w pojedynczej serii w danym dniu."
        )
        
        Text(
            text = "Wynik = Ciężar × Powtórzenia",
            color = Color(0xFF557D45),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 8.dp)
        )
        
        MethodologyItem(
            title = "Wykres Tygodniowy",
            description = "Suma objętości wszystkich treningów wykonanych danego dnia (Total Volume)."
        )
    }
}

@Composable
fun MethodologyItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(title, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Color.Gray, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
fun WeeklyProgressChart(weeklyData: List<WeeklyChartPoint>) {
    val maxVolume = weeklyData.maxOfOrNull { it.totalVolume }?.takeIf { it > 0 } ?: 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aktywność w ostatnim tygodniu",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { point ->
                    BarItem(
                        point = point,
                        heightPercentage = (point.totalVolume / maxVolume).toFloat()
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.BarItem(point: WeeklyChartPoint, heightPercentage: Float) {
    Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Wartość nad słupkiem
        if (point.totalVolume > 0) {
            Text(
                text = "${(point.totalVolume / 1000).toInt()}k",
                fontSize = 9.sp,
                color = Color(0xFF557D45),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Kontener na słupek (Fix: ten Box zajmuje resztę miejsca i trzyma słupek na dole)
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(heightPercentage.coerceIn(0.05f, 1f))
                    .width(24.dp)
                    .background(
                        color = if (point.totalVolume > 0) Color(0xFF557D45) else Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dzień tygodnia (Teraz zawsze na dole)
        Text(
            text = point.dayName,
            fontSize = 10.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TrainingProgressCard(card: TrainingProgressCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9))
    ) {
        Column {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF557D45),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Text(
                    text = card.trainingName.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                card.exercises.forEachIndexed { index, exercise ->
                    ExerciseComparisonRow(exercise)
                    if (index < card.exercises.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseComparisonRow(exercise: ExerciseComparison) {
    Column {
        Text(
            text = exercise.exerciseName,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatColumn("Miesiąc temu", exercise.monthAgoMax)
            StatColumn("Tydzień temu", exercise.weekAgoMax)
            StatColumn("Dziś", exercise.todayMax, isCurrent = true)
        }
    }
}

@Composable
fun StatColumn(label: String, value: Double, isCurrent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.DarkGray)
        Text(
            text = if (value > 0.0) "${value.toInt()} kg" else "---",
            fontSize = 14.sp,
            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isCurrent) Color(0xFF557D45) else Color.Black
        )
    }
}
