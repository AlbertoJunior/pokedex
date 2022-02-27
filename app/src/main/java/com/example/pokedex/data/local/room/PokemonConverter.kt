package com.example.pokedex.data.local.room

import androidx.room.TypeConverter
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.data.remote.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PokemonConverter {
    @TypeConverter
    fun abilityFromString(value: String?): Ability? {
        return Gson().fromJson(value, Ability::class.java)
    }

    @TypeConverter
    fun abilityToString(value: Ability?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun abilitiesFromString(value: String?): List<Ability>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<Ability>>() {}.type)
    }

    @TypeConverter
    fun abilityToString(value: List<Ability>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun moveFromString(value: String?): Move? {
        return Gson().fromJson(value, Move::class.java)
    }

    @TypeConverter
    fun moveToString(value: Move?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun movesFromString(value: String?): List<Move>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<Move>>() {}.type)
    }

    @TypeConverter
    fun moveToString(value: List<Move>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun statFromString(value: String?): Stat? {
        return Gson().fromJson(value, Stat::class.java)
    }

    @TypeConverter
    fun statToString(value: Stat?): String? {
        return Gson().toJson(value)
    }

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
    fun typeFromString(value: String?): Type? {
        return Gson().fromJson(value, Type::class.java)
    }

    @TypeConverter
    fun typeToString(value: Type?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun typesFromString(value: String?): List<Type>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<Type>>() {}.type)
    }

    @TypeConverter
    fun typesToString(value: List<Type>?): String? {
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
    fun flavorTextEntriesToString(value: List<FlavorTextEntry>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun flavorTextEntriesFromString(value: String?): List<FlavorTextEntry>? {
        if (value == null)
            return emptyList()
        return Gson().fromJson(value, object : TypeToken<List<FlavorTextEntry>>() {}.type)
    }

}