package com.example.pokedex.data.local.room

import androidx.room.TypeConverter
import com.example.pokedex.data.local.model.PokemonArea
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.data.remote.model.NameUrlObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PokemonConverter {
    @TypeConverter
    fun statsFromString(value: String?): List<Stat>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<Stat>>() {}.type)
    }

    @TypeConverter
    fun statsToString(value: List<Stat>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun stringsToString(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun spritesFromString(value: String?): List<String>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun nameUrlFromString(value: String?): NameUrlObject? {
        return if (value == null) null else Gson().fromJson(value, NameUrlObject::class.java)
    }

    @TypeConverter
    fun nameUrlToString(value: NameUrlObject?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun pokemonSpecieFromString(value: String?): PokemonSpecie? {
        return if (value == null) null else Gson().fromJson(value, PokemonSpecie::class.java)
    }

    @TypeConverter
    fun pokemonSpecieToString(value: PokemonSpecie?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun encounterAreasToString(value: List<PokemonArea>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun encounterAreasFromString(value: String?): List<PokemonArea>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<PokemonArea>>() {}.type)
    }

}