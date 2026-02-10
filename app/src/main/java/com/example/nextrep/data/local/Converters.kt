package com.example.nextrep.data.local

import androidx.room.TypeConverter
import com.example.nextrep.domain.models.Exercise
import com.example.nextrep.domain.models.DayOfWeek
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromExerciseList(value: MutableList<Exercise>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toExerciseList(value: String?): MutableList<Exercise>? {
        if (value == null) return null
        val listType = object : TypeToken<MutableList<Exercise>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromDaySet(value: Set<DayOfWeek>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toDaySet(value: String?): Set<DayOfWeek>? {
        if (value == null) return null
        val setType = object : TypeToken<Set<DayOfWeek>>() {}.type
        return gson.fromJson(value, setType)
    }
}