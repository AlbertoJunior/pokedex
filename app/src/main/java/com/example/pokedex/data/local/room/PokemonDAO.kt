package com.example.pokedex.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonSpecie

@Dao
interface PokemonDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: Pokemon): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemon: List<Pokemon>)

    @Query("SELECT * FROM Pokemon WHERE `offset` = :offset ORDER BY id ASC LIMIT :quantity")
    fun fetchPokemons(offset: Int = 0, quantity: Int = 20): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon ORDER BY id ASC LIMIT 100")
    fun getAllPokemon(): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon WHERE favorite = 1 ORDER BY id ASC")
    fun getAllFavoritePokemon(): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon WHERE id =:id")
    fun fetchPokemonById(id: Long): LiveData<Pokemon>

    @Query("SELECT * FROM Pokemon WHERE id =:id")
    suspend fun fetchDirectPokemonById(id: Long): Pokemon

    @Query("SELECT COUNT(id) FROM Pokemon WHERE `offset` = :offset LIMIT 1")
    suspend fun hasPokemon(offset: Int): Boolean

    @Query("UPDATE Pokemon SET favorite = :isFavorite WHERE id = :pokemonId")
    suspend fun favoritePokemon(pokemonId: Long, isFavorite: Boolean)

    @Query("UPDATE Pokemon SET " +
            "baseHappiness = :pokemonSpecie-baseHappiness, " +
            "captureRate = :pokemonSpecie-captureRate, " +
            "color = :pokemonSpecie-color, " +
            "flavorTextEntries = :pokemonSpecie-flavorTextEntries, " +
            "growthRate = :pokemonSpecie-growthRate, " +
            "habitat = :pokemonSpecie-habitat, " +
            "isBaby = :pokemonSpecie-isBaby, " +
            "isLegendary = :pokemonSpecie-isLegendary, " +
            "isMythical = :pokemonSpecie-isMythical, " +
            "shape = :pokemonSpecie-shape " +
            "WHERE id = :pokemonId")
    suspend fun updatePokemonSpecie(pokemonId: Long, pokemonSpecie: PokemonSpecie)
}