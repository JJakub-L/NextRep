package com.example.nextrep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.nextrep.data.MockWorkoutRepository
import com.example.nextrep.presentation.MainScreen
import com.example.nextrep.presentation.WorkoutViewModel
import com.example.nextrep.ui.theme.NextRepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Tworzymy bazę danych (Repository)
        val repository = MockWorkoutRepository()

        // 2. Tworzymy ViewModel i WSTRZYKUJEMY do niego bazę danych (DI!)
        val viewModel = WorkoutViewModel(repository)

        setContent {
            NextRepTheme {
                // 3. Wyświetlamy nasz ekran
                MainScreen(viewModel = viewModel)
            }
        }
    }
}