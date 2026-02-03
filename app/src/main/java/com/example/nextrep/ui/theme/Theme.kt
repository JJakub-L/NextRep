package com.example.nextrep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Definiujemy, że nasza aplikacja używa TYLKO ciemnych kolorów
private val DarkColorScheme = darkColorScheme(
    primary = GorillaGreen,
    secondary = BrightGreen,
    background = GymBlack,
    surface = GymCard,
    onPrimary = WhiteText,
    onBackground = WhiteText,
    onSurface = WhiteText
)

@Composable
fun NextRepTheme(
    content: @Composable () -> Unit
) {
    // Tutaj normalnie jest sprawdzanie "isSystemInDarkTheme()",
    // ale my usuwamy warunek i wymuszamy DarkColorScheme na stałe.
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}