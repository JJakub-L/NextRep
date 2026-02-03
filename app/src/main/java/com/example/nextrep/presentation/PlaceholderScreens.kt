package com.example.nextrep.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// To są tymczasowe ekrany, żeby nawigacja działała.
// Później wypełnimy je prawdziwą treścią.

@Composable
fun ProgressScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Tu będą wykresy i statystyki 📈")
    }
}

@Composable
fun AddPlanScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Tu będziesz dodawać nowe plany 📝")
    }
}