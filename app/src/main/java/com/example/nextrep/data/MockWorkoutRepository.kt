package com.example.nextrep.data

import com.example.nextrep.domain.WorkoutRepository
import com.example.nextrep.domain.models.*

class MockWorkoutRepository : WorkoutRepository {

    private val workouts = mutableListOf<Workout>()

    init {
        createTrainingA()
    }

    override fun getAllWorkouts(): List<Workout> {
        return workouts
    }

    override fun saveWorkout(workout: Workout) {
        // Zapis
    }

    private fun createTrainingA() {
        val exercises = mutableListOf<Exercise>()

        // 1. Wyciskanie
        val benchPressSets = mutableListOf<ExerciseSet>()
        for (i in 1..3) {
            benchPressSets.add(
                ExerciseSet(
                    setNumber = i, type = SetType.WARMUP,
                    targetReps = "5", targetRir = "-",
                    weightInput = "100", repsInput = "5"
                )
            )
        }
        for (i in 1..3) {
            benchPressSets.add(
                ExerciseSet(
                    setNumber = i, type = SetType.WORKING,
                    targetReps = "5", targetRir = "2",
                    weightInput = "", repsInput = ""
                )
            )
        }
        exercises.add(Exercise(name = "Wyciskanie sztangi na ławce", sets = benchPressSets))

        // Pozostałe
        exercises.add(createNormalExercise("Rozpiętki na maszynie", 3, "10-12", "1"))
        exercises.add(createNormalExercise("Lat Pulldown", 3, "8-10", "1"))
        exercises.add(createNormalExercise("Wznosy bokiem na linkach", 3, "10-12", "1"))
        exercises.add(createNormalExercise("Reverse cable fly", 3, "10-12", "1"))

        val trainingA = Workout(
            name = "Trening A",
            dayDescription = "Poniedziałek",
            exercises = exercises,
        )
        workouts.add(trainingA)
    }

    private fun createNormalExercise(name: String, setsCount: Int, reps: String, rir: String): Exercise {
        val sets = mutableListOf<ExerciseSet>()
        for (i in 1..setsCount) {
            sets.add(
                ExerciseSet(
                    setNumber = i, type = SetType.WORKING,
                    targetReps = reps, targetRir = rir,
                    weightInput = "", repsInput = ""
                )
            )
        }
        return Exercise(name = name, sets = sets)
    }
}