package com.example.pokedex.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pokedex.data.local.model.Pokemon

@Database(
    entities = [Pokemon::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(PokemonConverter::class)
abstract class PokemonDatabase : RoomDatabase() {
    abstract val pokemonDAO: PokemonDAO
}