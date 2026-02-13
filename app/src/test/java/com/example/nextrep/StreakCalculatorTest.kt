package com.example.nextrep

import com.example.nextrep.domain.models.DayOfWeek
import com.example.nextrep.domain.models.Workout
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class StreakCalculatorTest {

    // Logika skopiowana z ViewModelu do testów
    private fun calculateStreak(workouts: List<Workout>, testCurrentTime: Long? = null): Int {
        val completedDatesSet = workouts
            .filter { it.isCompleted && it.completionDate != null }
            .map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.completionDate!! }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }
            .toSet()

        val allScheduledDays = workouts.flatMap { it.scheduledDays }.toSet()
        if (allScheduledDays.isEmpty() && completedDatesSet.isEmpty()) return 0

        var streak = 0
        val calendar = Calendar.getInstance().apply {
            if (testCurrentTime != null) timeInMillis = testCurrentTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (i in 0 until 365) {
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
            val dayOfWeek = calendarDayToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))
            val isScheduled = allScheduledDays.contains(dayOfWeek)
            val isCompleted = completedDatesSet.contains(dateKey)

            if (isCompleted) {
                streak++
            } else if (isScheduled) {
                if (i > 0) {
                    break 
                }
            } else {
                // Rest day - pauza
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun calendarDayToDayOfWeek(calendarDay: Int): DayOfWeek {
        return when (calendarDay) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.SUNDAY
        }
    }

    @Test
    fun `test streak with rest day stays same`() {
        // Scenariusz: Trening w Poniedziałek i Środę. Czwartek to Rest Day.
        // Dzisiaj jest Czwartek.
        val calendar = Calendar.getInstance()
        // Ustawiamy "dzisiaj" na konkretny czwartek
        calendar.set(2023, Calendar.OCTOBER, 26) // 26 Oct 2023 is Thursday
        val today = calendar.timeInMillis
        
        calendar.set(2023, Calendar.OCTOBER, 25) // Wednesday
        val wednesday = calendar.timeInMillis
        
        calendar.set(2023, Calendar.OCTOBER, 23) // Monday
        val monday = calendar.timeInMillis

        val workoutPlan = Workout(
            name = "Test Plan",
            dayDescription = "",
            scheduledDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val completedWorkouts = listOf(
            workoutPlan.copy(isCompleted = true, completionDate = monday),
            workoutPlan.copy(isCompleted = true, completionDate = wednesday)
        )

        val result = calculateStreak(completedWorkouts, testCurrentTime = today)
        assertEquals("Streak powinien wynosić 2 (Poniedziałek i Środa), Czwartek (Rest) nie przerywa", 2, result)
    }

    @Test
    fun `test streak breaks on missed scheduled day`() {
        // Scenariusz: Trening w Poniedziałek. Środa planowana ale pominięta.
        // Dzisiaj jest Czwartek.
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.OCTOBER, 26) // Thursday
        val today = calendar.timeInMillis
        
        calendar.set(2023, Calendar.OCTOBER, 23) // Monday
        val monday = calendar.timeInMillis

        val workoutPlan = Workout(
            name = "Test Plan",
            dayDescription = "",
            scheduledDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        )

        val completedWorkouts = listOf(
            workoutPlan.copy(isCompleted = true, completionDate = monday)
        )

        val result = calculateStreak(completedWorkouts, testCurrentTime = today)
        assertEquals("Streak powinien wynosić 0 (lub zostać przerwany), bo Środa była planowana a nie zrobiona", 0, result)
    }
}
