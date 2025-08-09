package com.example.shuttlereg.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Gson().fromJson<List<String>>(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return try {
            Gson().fromJson<Map<String, String>>(value, object : TypeToken<Map<String, String>>() {}.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    @TypeConverter
    fun fromDoubleMap(value: Map<String, Double>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toDoubleMap(value: String): Map<String, Double> {
        return try {
            Gson().fromJson<Map<String, Double>>(value, object : TypeToken<Map<String, Double>>() {}.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}