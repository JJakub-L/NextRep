package com.example.nextrep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.nextrep.data.local.AppDatabase
import com.example.nextrep.presentation.MainScreen
import com.example.nextrep.presentation.WorkoutViewModel
import com.example.nextrep.ui.theme.NextRepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Tworzymy bazę danych
        val dao = AppDatabase.getDatabase(this).workoutDao()

        // 2. Tworzymy ViewModel i WSTRZYKUJEMY do niego bazę danych (DI!)
        val viewModel = WorkoutViewModel(dao)

        setContent {
            NextRepTheme {
                // 3. Wyświetlamy nasz ekran
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
