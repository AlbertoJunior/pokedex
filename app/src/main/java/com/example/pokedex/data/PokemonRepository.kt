package com.example.pokedex.data

import android.util.Log
import androidx.lifecycle.liveData
import com.example.pokedex.core.capitalize
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonArea
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.data.local.room.PokemonDAO
import com.example.pokedex.data.remote.PokemonAPI
import kotlinx.coroutines.*
import java.sql.SQLException
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonAPI: PokemonAPI,
    private val pokemonDAO: PokemonDAO
) {

    fun fetchListPokemonLocal(offset: Int = 0, quantity: Int = 10) = liveData {
        emitSource(pokemonDAO.getAllPokemon())
        fetchListOnline(offset, quantity)
    }

    fun fetchFavoritePokemonsLocal() = liveData {
        emitSource(pokemonDAO.getAllFavoritePokemon())
    }

    private suspend fun fetchListOnline(offset: Int, quantity: Int) {
        try {
            val hasPokemon = pokemonDAO.hasPokemon(offset)
            if (!hasPokemon) {
                val fetchPokemonList = pokemonAPI.fetchPokemonList(offset, quantity)
                val pokemonList = fetchPokemonList.results.map {
                    val id = it.url
                        .replace(PokemonAPI.BASE_URL, "")
                        .replace("pokemon", "")
                        .replace("/", "")
                    Pokemon(id.toLong(), it.name, offset)
                }
                pokemonDAO.insertPokemons(pokemonList)
                fetchPokemonDetails(pokemonList)
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchListOnline")
        }
    }

    private suspend fun fetchPokemonDetails(pokemonList: List<Pokemon>) {
        withContext(Dispatchers.IO) {
            val mapAsync = pokemonList.map { async { fetchPokemonOnline(it.id) } }
            val awaitAll = mapAsync.awaitAll().filterNotNull()
            pokemonDAO.insertPokemons(awaitAll)
        }
    }

    suspend fun fetchPokemonDirect(pokemonId: Long): Pokemon? {
        return try {
            pokemonDAO.fetchDirectPokemonById(pokemonId)
        } catch (e: SQLException) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonDirect")
            null
        }
    }

    fun fetchPokemonLocal(pokemonId: Long, allDetails: Boolean = false) = liveData {
        emitSource(pokemonDAO.fetchPokemonById(pokemonId))
        withContext(Dispatchers.IO) {
            mutableListOf<Deferred<Any?>>().apply {
                add(async { fetchPokemonOnline(pokemonId)?.let { pokemonDAO.insertPokemon(it) } })
                if (allDetails) {
                    add(async { fetchPokemonOnlineAllDetails(pokemonId) })
                    add(async { fetchPokemonOnlineEncounterArea(pokemonId) })
                }
            }.awaitAll()
        }
    }

    private suspend fun fetchPokemonOnline(pokemonId: Long): Pokemon? {
        return try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            return if (!directPokemon.isComplete()) {
                val fetchPokemonById = pokemonAPI.fetchPokemonById(pokemonId)

                val listSprites: List<String> = mutableListOf<String>().apply {
                    fetchPokemonById.sprites?.other?.let { other ->
                        other.dreamWorld?.frontDefault?.let { dreamWorld ->
                            add(dreamWorld)
                        }
                        other.home?.frontDefault?.let { home ->
                            add(home)
                        }
                        other.officialArtwork?.frontDefault?.let { official ->
                            add(official)
                        }
                    }
                }

                val stats = fetchPokemonById.stats.mapNotNull {
                    return@mapNotNull if (it.stat?.name != null) {
                        Stat(it.stat.name, it.baseStat ?: 0L, it.effort ?: 0L)
                    } else {
                        null
                    }
                }

                val types = fetchPokemonById.types.mapNotNull { it.type?.name }.distinct()

                val moves = fetchPokemonById.moves
                    .mapNotNull {
                        it.move?.name?.replace("-", " ")?.capitalize()
                    }
                    .distinct()

                val pokemon = Pokemon(
                    pokemonId,
                    directPokemon.name,
                    directPokemon.offset,
                    fetchPokemonById.abilities,
                    moves,
                    fetchPokemonById.height,
                    fetchPokemonById.locationAreaEncounters,
                    fetchPokemonById.baseExperience,
                    fetchPokemonById.species,
                    stats,
                    listSprites,
                    types,
                    fetchPokemonById.weight
                )
                pokemon
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnline")
            null
        }
    }

    private suspend fun fetchPokemonOnlineAllDetails(pokemonId: Long) {
        try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            if (directPokemon.pokemonSpecie == null && directPokemon.species?.url?.isNotEmpty() == true) {
                val fetchSpeciePokemonById = pokemonAPI.fetchSpeciePokemonById(pokemonId)

                val flavorTextEntries = fetchSpeciePokemonById.flavorTextEntries
                    ?.filter { it.language?.name == "en" }
                    ?.mapNotNull { it.flavorText }
                    ?.map { it.replace("[\n\\f]+".toRegex(), " ") }
                    ?.distinctBy { it }

                val pokemonSpecie = PokemonSpecie(
                    fetchSpeciePokemonById.baseHappiness,
                    fetchSpeciePokemonById.captureRate,
                    fetchSpeciePokemonById.color?.name,
                    flavorTextEntries,
                    fetchSpeciePokemonById.growthRate?.name,
                    fetchSpeciePokemonById.habitat?.name,
                    fetchSpeciePokemonById.isBaby,
                    fetchSpeciePokemonById.isLegendary,
                    fetchSpeciePokemonById.isMythical,
                    fetchSpeciePokemonById.shape?.name
                )
                pokemonDAO.updatePokemonSpecie(pokemonId, pokemonSpecie)
            }
        } catch (e: SQLException) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineAllDetails")
        }
    }

    private suspend fun fetchPokemonOnlineEncounterArea(pokemonId: Long) {
        try {
            val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
            if (directPokemon.locationAreaEncounters != null && directPokemon.pokemonArea.isEmpty()) {
                val encounterAreaPokemon = pokemonAPI.fetchEncounterAreaPokemonById(pokemonId)

                val areas = encounterAreaPokemon.mapNotNull {
                    it.locationArea?.name?.let { nameArea ->
                        it.versionDetails
                            ?.firstOrNull()?.encounterDetails?.firstOrNull()
                            ?.let { encounterDetail ->
                                PokemonArea(
                                    nameArea,
                                    encounterDetail.chance,
                                    encounterDetail.minLevel,
                                    encounterDetail.maxLevel
                                )
                            }
                    }
                }
                pokemonDAO.updatePokemonArea(pokemonId, areas)
            }
        } catch (e: SQLException) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineEncounterArea")
        }
    }

    suspend fun savePokemonInFavorites(pokemonId: Long, isFavorite: Boolean) {
        pokemonDAO.favoritePokemon(pokemonId, isFavorite)
    }

}