package com.example.pokedex.data.local.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonArea
import com.example.pokedex.data.local.model.PokemonSpecie

@Dao
interface PokemonDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: Pokemon): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemon: List<Pokemon>)

    @Query("SELECT * FROM Pokemon WHERE `offset` = :offset ORDER BY id ASC LIMIT :quantity")
    fun fetchPokemonList(offset: Int, quantity: Int): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon ORDER BY id ASC")
    fun fetchPokemonPaging(): PagingSource<Int, Pokemon>

    @Query("SELECT * FROM Pokemon ORDER BY id ASC LIMIT 100")
    fun fetchAllPokemon(): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon WHERE favorite = 1 ORDER BY id ASC")
    fun fetchAllFavoritePokemon(): LiveData<List<Pokemon>>

    @Query("SELECT * FROM Pokemon WHERE id =:id")
    fun fetchPokemonById(id: Long): LiveData<Pokemon>

    @Query("SELECT * FROM Pokemon WHERE name =:name")
    fun fetchPokemonByName(name: String): LiveData<Pokemon>

    @Query("SELECT * FROM Pokemon WHERE id =:id")
    suspend fun fetchDirectPokemonById(id: Long): Pokemon

    @Query("SELECT * FROM Pokemon WHERE name =:name")
    suspend fun fetchDirectPokemonByName(name: String): Pokemon?

    @Query("SELECT COUNT(id) FROM Pokemon WHERE `offset` = :offset LIMIT 1")
    suspend fun hasPokemon(offset: Int): Boolean

    @Query("UPDATE Pokemon SET favorite = :isFavorite WHERE id = :pokemonId")
    suspend fun favoritePokemon(pokemonId: Long, isFavorite: Boolean)

    @Query("UPDATE Pokemon SET pokemonSpecie = :pokemonSpecie WHERE id = :pokemonId")
    suspend fun updatePokemonSpecie(pokemonId: Long, pokemonSpecie: PokemonSpecie)

    @Query("UPDATE Pokemon SET pokemonArea = :pokemonArea WHERE id = :pokemonId")
    suspend fun updatePokemonArea(pokemonId: Long, pokemonArea: List<PokemonArea>)
}