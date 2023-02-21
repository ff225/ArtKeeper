package com.example.artkeeper.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromListToString(nameChild: List<String>?): String? {
        return Gson().toJson(nameChild)
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<String>? {
        val listType = object :
            TypeToken<List<String?>?>() {}.type
        return Gson()
            .fromJson<List<String>>(value, listType)
    }
}