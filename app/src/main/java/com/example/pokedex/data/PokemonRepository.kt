package com.example.pokedex.data

import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.pokedex.core.EventSource
import com.example.pokedex.core.capitalize
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonArea
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.room.PokemonDAO
import com.example.pokedex.data.remote.ConverterRemote
import com.example.pokedex.data.remote.PokemonAPI
import kotlinx.coroutines.*
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonAPI: PokemonAPI,
    private val pokemonDAO: PokemonDAO
) {
    suspend fun hasPokemon(offset: Int) = pokemonDAO.hasPokemon(offset)

    fun pokemonListAllPaging() = pokemonDAO.fetchPokemonPaging()

    suspend fun insertPokemonList(pokemon: List<Pokemon>) = pokemonDAO.insertPokemons(pokemon)

    fun fetchFavoritePokemonLocal() = liveData { emitSource(pokemonDAO.fetchAllFavoritePokemon()) }

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

    suspend fun safeFetchPokemonDirect(pokemonId: Long): Pokemon? {
        return try {
            pokemonDAO.fetchDirectPokemonById(pokemonId)
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonDirect")
            null
        }
    }

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
                add(async {
                    fetchPokemonLocalOrOnline(pokemonId)
                })
                add(async {
                    fetchPokemonOnlineAllDetails(pokemonId)?.let { pokemonSpecie ->
                        pokemonDAO.updatePokemonSpecie(pokemonId, pokemonSpecie)
                    }
                })
                add(async {
                    fetchPokemonOnlineEncounterArea(pokemonId).let { areas ->
                        pokemonDAO.updatePokemonArea(pokemonId, areas)
                    }
                })
            }.awaitAll()
        }
    }

    private suspend fun fetchPokemonLocalOrOnline(pokemonId: Long): Pokemon? {
        val directPokemon = pokemonDAO.fetchDirectPokemonByIdNullable(pokemonId)
        return if (directPokemon == null || !directPokemon.isComplete()) {
            fetchPokemonOnline(pokemonId)?.let { pokemon ->
                pokemonDAO.insertPokemon(pokemon)
                pokemon
            }
        } else {
            directPokemon
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

    private suspend fun fetchPokemonOnlineAllDetails(pokemonId: Long): PokemonSpecie? {
        try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            if (directPokemon.pokemonSpecie == null && directPokemon.specie?.url?.isNotEmpty() == true) {
                val fetchSpeciePokemonById = pokemonAPI.fetchSpeciePokemonById(pokemonId)

                val flavorTextEntries = ConverterRemote.flavorText(fetchSpeciePokemonById)
                val habitat = fetchSpeciePokemonById.habitat?.name?.replace("-", " ")?.capitalize()
                val shape = fetchSpeciePokemonById.shape?.name?.replace("-", " ")?.capitalize()

                return PokemonSpecie(
                    fetchSpeciePokemonById.baseHappiness,
                    fetchSpeciePokemonById.captureRate,
                    fetchSpeciePokemonById.color?.name,
                    flavorTextEntries,
                    fetchSpeciePokemonById.growthRate?.name,
                    habitat,
                    fetchSpeciePokemonById.isBaby,
                    fetchSpeciePokemonById.isLegendary,
                    fetchSpeciePokemonById.isMythical,
                    shape
                )
            } else if (directPokemon.pokemonSpecie != null) {
                return directPokemon.pokemonSpecie
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineAllDetails")
        }
        return null
    }

    private suspend fun fetchPokemonOnlineEncounterArea(pokemonId: Long): List<PokemonArea> {
        try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            if (directPokemon.locationAreaEncounters != null && directPokemon.pokemonArea.isEmpty()) {
                val encounterAreaPokemon = pokemonAPI.fetchEncounterAreaPokemonById(pokemonId)
                return ConverterRemote.pokemonArea(encounterAreaPokemon)
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineEncounterArea")
        }
        return emptyList()
    }

    suspend fun savePokemonInFavorites(pokemonId: Long, isFavorite: Boolean) {
        pokemonDAO.favoritePokemon(pokemonId, isFavorite)
    }

    suspend fun sendPokemonInfo(pokemonId: Long): Pokemon {
        pokemonDAO.fetchDirectPokemonById(pokemonId).let { pokemon ->
            pokemonAPI.sendPokemonInfo(pokemon)
            return pokemon
        }
    }

}