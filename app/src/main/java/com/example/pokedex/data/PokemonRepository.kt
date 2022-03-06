package com.example.pokedex.data

import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonArea
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.room.PokemonDAO
import com.example.pokedex.data.paging.PokemonRemotePagingSource
import com.example.pokedex.data.remote.ConverterRemote
import com.example.pokedex.data.remote.PokemonAPI
import kotlinx.coroutines.*
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonAPI: PokemonAPI,
    private val pokemonDAO: PokemonDAO
) {

    @OptIn(ExperimentalPagingApi::class)
    val pokemonListAllPaging = Pager(
        PagingConfig(10, initialLoadSize = 10, enablePlaceholders = true),
        remoteMediator = PokemonRemotePagingSource(this)
    ) {
        pokemonDAO.fetchPokemonPaging()
    }.flow

    suspend fun savePokemonInFavorites(pokemonId: Long, isFavorite: Boolean) {
        pokemonDAO.favoritePokemon(pokemonId, isFavorite)
    }

    suspend fun sendPokemonInfo(pokemonId: Long) =
        pokemonDAO.fetchDirectPokemonById(pokemonId).let { pokemon ->
            pokemonAPI.sendPokemonInfo(pokemon)
            pokemon
        }

    suspend fun hasPokemon(offset: Int) = pokemonDAO.hasPokemon(offset)

    suspend fun insertPokemonList(pokemon: List<Pokemon>) = pokemonDAO.insertPokemons(pokemon)

    fun fetchFavoritePokemonLocal() = pokemonDAO.fetchAllFavoritePokemon()

    suspend fun fetchListOnline(offset: Int, quantity: Int = 10): List<Pokemon> {
        try {
            val fetchPokemonList = pokemonAPI.fetchPokemonList(offset, quantity)
            return fetchPokemonList.results.map {
                val id = ConverterRemote.id(it)
                val name = ConverterRemote.name(it.name)
                Pokemon(id, name, offset)
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchListOnline")
        }
        return emptyList()
    }

    suspend fun fetchPokemonDetails(pokemonList: List<Pokemon>): List<Pokemon> {
        return withContext(Dispatchers.IO) {
            val mapAsync = pokemonList.map {
                async {
                    fetchPokemonLocalOrOnline(it.id)
                }
            }
            mapAsync.awaitAll().filterNotNull()
        }
    }

    suspend fun safeFetchPokemonDirect(pokemonId: Long) =
        pokemonDAO.fetchDirectPokemonByIdNullable(pokemonId)

    fun searchPokemonByNameAllDetailsLocal(pokemonName: String) = liveData<EventSource<Pokemon?>> {
        withContext(Dispatchers.IO) {
            try {
                emit(EventSource.Loading("Searching Pokemon"))

                val directPokemon = pokemonDAO.fetchDirectPokemonByName(pokemonName)
                val data = if (directPokemon == null) {
                    val fetchPokemonByName = pokemonAPI.fetchPokemonByName(pokemonName)
                    val pokemon = ConverterRemote.pokemon(fetchPokemonByName)
                    pokemonDAO.insertPokemon(pokemon)
                    fetchPokemonAllDetailsLocal(pokemon.id)
                } else {
                    fetchPokemonAllDetailsLocal(directPokemon.id)
                }
                emitSource(data.map { pokemon -> EventSource.Ready(pokemon) })
            } catch (e: Exception) {
                Log.e("PokemonRepository", e.message ?: "fetchPokemonOnline")
                emit(EventSource.Error(e.message))
            }
        }
    }

    fun searchPokemonByIdAllDetailsLocal(pokemonId: Long) = liveData<EventSource<Pokemon?>> {
        withContext(Dispatchers.IO) {
            try {
                emit(EventSource.Loading("Searching Pokemon"))

                val directPokemon = pokemonDAO.fetchDirectPokemonByIdNullable(pokemonId)
                val data = if (directPokemon == null) {
                    val fetchPokemonByName = pokemonAPI.fetchPokemonById(pokemonId)
                    val pokemon = ConverterRemote.pokemon(fetchPokemonByName)
                    pokemonDAO.insertPokemon(pokemon)
                    fetchPokemonAllDetailsLocal(pokemon.id)
                } else {
                    fetchPokemonAllDetailsLocal(directPokemon.id)
                }

                emitSource(data.map { pokemon -> EventSource.Ready(pokemon) })
            } catch (e: Exception) {
                Log.e("PokemonRepository", e.message ?: "fetchPokemonOnline")
                emit(EventSource.Error(e.message))
            }
        }
    }

    fun fetchPokemonAllDetailsLocal(pokemonId: Long) = liveData {
        emitSource(pokemonDAO.fetchPokemonById(pokemonId))
        withContext(Dispatchers.IO) {
            mutableListOf<Deferred<Any?>>().apply {
                add(async { fetchPokemonLocalOrOnline(pokemonId) })
                add(async { fetchPokemonAllDetailsLocalOrOnline(pokemonId) })
                add(async { fetchPokemonAreaLocalOrOnline(pokemonId) })
            }.awaitAll()
        }
    }

    private suspend fun fetchPokemonLocalOrOnline(pokemonId: Long): Pokemon? {
        return try {
            val directPokemon = pokemonDAO.fetchDirectPokemonByIdNullable(pokemonId)
            if (directPokemon == null || !directPokemon.isComplete()) {
                fetchPokemonOnline(pokemonId)?.let { pokemon ->
                    pokemonDAO.insertPokemon(pokemon)
                    pokemon
                }
            } else {
                directPokemon
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonLocalOrOnline")
            null
        }
    }

    private suspend fun fetchPokemonOnline(pokemonId: Long): Pokemon? {
        return try {
            ConverterRemote.pokemon(pokemonAPI.fetchPokemonById(pokemonId))
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnline")
            null
        }
    }

    private suspend fun fetchPokemonAllDetailsLocalOrOnline(pokemonId: Long): PokemonSpecie? {
        try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            if (directPokemon.pokemonSpecie != null) {
                return directPokemon.pokemonSpecie
            } else if (directPokemon.specie?.url?.isNotEmpty() == true) {
                fetchPokemonAllDetailsOnline(pokemonId)?.let { pokemonSpecie ->
                    pokemonDAO.updatePokemonSpecie(pokemonId, pokemonSpecie)
                    return pokemonSpecie
                }
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonAllDetailsLocalOrOnline")
        }
        return null
    }

    private suspend fun fetchPokemonAllDetailsOnline(pokemonId: Long): PokemonSpecie? {
        return try {
            ConverterRemote.pokemonSpecie(pokemonAPI.fetchSpeciePokemonById(pokemonId))
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineAllDetails")
            null
        }
    }

    private suspend fun fetchPokemonAreaLocalOrOnline(pokemonId: Long): List<PokemonArea> {
        return try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            if (directPokemon.locationAreaEncounters != null && directPokemon.pokemonArea.isEmpty()) {
                fetchPokemonOnlineEncounterArea(pokemonId).let { areas ->
                    pokemonDAO.updatePokemonArea(pokemonId, areas)
                    areas
                }
            } else {
                directPokemon.pokemonArea
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonAreaLocalOrOnline")
            emptyList()
        }
    }

    private suspend fun fetchPokemonOnlineEncounterArea(pokemonId: Long): List<PokemonArea> {
        return try {
            ConverterRemote.pokemonArea(pokemonAPI.fetchEncounterAreaPokemonById(pokemonId))
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineEncounterArea")
            emptyList()
        }
    }

}