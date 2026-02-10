package com.example.nextrep.data.local

import androidx.room.*
import com.example.nextrep.domain.models.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_plans")
    fun getAllPlans(): Flow<List<Workout>>

    // Zmieniamy na Long - zwróci ID nowo wstawionego wiersza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    // Zmieniamy na Int - zwróci liczbę usuniętych wierszy (zazwyczaj 1)
    @Delete
    suspend fun deleteWorkout(workout: Workout): Int
}