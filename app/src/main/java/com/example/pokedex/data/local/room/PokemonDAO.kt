package com.example.pokedex.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.pokedex.data.local.model.Pokemon

@Dao
interface PokemonDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPokemon(pokemon: Pokemon): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPokemons(pokemon: List<Pokemon>)

    @Query("SELECT * FROM Pokemon WHERE `offset` = :offset ORDER BY id ASC LIMIT :quantity")
    fun fetchPokemons(offset: Int = 0, quantity: Int = 20): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon WHERE id =:id")
    fun fetchPokemonById(id: Long): LiveData<Pokemon>

    @Query("SELECT * FROM Pokemon WHERE id =:id")
    fun fetchDirectPokemonById(id: Long): Pokemon

    @Query("SELECT COUNT(id) FROM Pokemon WHERE `offset` = :offset")
    fun hasPokemons(offset: Int): Boolean
}