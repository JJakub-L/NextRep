package com.example.nextrep.domain // <--- Zwróć uwagę: pakiet DOMAIN

import com.example.nextrep.domain.models.Workout

interface WorkoutRepository {
    fun getAllWorkouts(): List<Workout>
    fun saveWorkout(workout: Workout)
}