package com.example.pokedex.data.local.room

import androidx.room.TypeConverter
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
        return Gson().fromJson(value, emptyList<Ability>().javaClass)
    }

    @TypeConverter
    fun abilityToString(value:  List<Ability>?): String? {
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
        return Gson().fromJson(value, emptyList<Move>().javaClass)
    }

    @TypeConverter
    fun moveToString(value:  List<Move>?): String? {
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
        return Gson().fromJson(value, emptyList<Stat>().javaClass)
    }

    @TypeConverter
    fun statsToString(value:  List<Stat>?): String? {
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
        return Gson().fromJson(value, emptyList<Type>().javaClass)
    }

    @TypeConverter
    fun typesToString(value:  List<Type>?): String? {
        return Gson().toJson(value)
    }


    @TypeConverter
    fun spritesFromString(value: String?): Sprites? {
        return Gson().fromJson(value, Sprites::class.java)
    }

    @TypeConverter
    fun spritesToString(value: Sprites?): String? {
        return Gson().toJson(value)
    }
}