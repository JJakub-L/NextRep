package com.example.nextrep.data.local

import androidx.room.*
import com.example.nextrep.domain.models.Workout
import kotlinx.coroutines.flow.Flow

/*
  WZORZEC PROJEKTOWY: DAO (Data Access Object)
  Oddziela logikę dostępu do danych (SQL) od reszty aplikacji.
  Definiuje czysty interfejs do komunikacji z bazą danych Room.
 */
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_plans")
    fun getAllPlans(): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Delete
    suspend fun deleteWorkout(workout: Workout): Int
}