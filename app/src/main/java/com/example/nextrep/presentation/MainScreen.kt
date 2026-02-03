package com.example.nextrep.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nextrep.ui.theme.GymBlack
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppScreen {
    Progress, Training, AddPlan
}

@Composable
fun MainScreen(viewModel: WorkoutViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.Training) }

    // TO JEST TO MIEJSCE O KTÓRE PYTAŁEŚ:
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBlack) // <--- Tu jest ustawione tło pod całą aplikacją
    ) {

        // --- 1. GÓRNY PANEL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B5E20))
        ) {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale("pl", "PL"))
            val dateString = today.format(formatter)

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = dateString.replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Trening: Klatka A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    // TUTAJ ZMIANA: Pobieramy wartość z viewModelu
                    text = "Streak: 🔥 ${viewModel.streak.value} dni",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 2. MENU NAWIGACYJNE ---
        TabRow(
            selectedTabIndex = currentScreen.ordinal,
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White
        ) {
            Tab(
                selected = currentScreen == AppScreen.Progress,
                onClick = { currentScreen = AppScreen.Progress },
                text = { Text("Postęp") }
            )
            Tab(
                selected = currentScreen == AppScreen.Training,
                onClick = { currentScreen = AppScreen.Training },
                text = { Text("Trening") }
            )
            Tab(
                selected = currentScreen == AppScreen.AddPlan,
                onClick = { currentScreen = AppScreen.AddPlan },
                text = { Text("Dodaj") }
            )
        }

        // --- 3. TREŚĆ ---
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            when (currentScreen) {
                AppScreen.Progress -> ProgressScreen()
                AppScreen.Training -> WorkoutScreenContent(viewModel)
                AppScreen.AddPlan -> AddPlanScreen()
            }
        }
    }
}