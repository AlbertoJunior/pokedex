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
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonAPI: PokemonAPI,
    private val pokemonDAO: PokemonDAO
) {
    suspend fun hasPokemon(offset: Int) = pokemonDAO.hasPokemon(offset)

    fun pokemonListAllPaging() = pokemonDAO.fetchPokemonPaging()

    suspend fun insertPokemonList(pokemon: List<Pokemon>) = pokemonDAO.insertPokemons(pokemon)

    fun fetchFavoritePokemonLocal() = liveData {
        emitSource(pokemonDAO.fetchAllFavoritePokemon())
    }

    suspend fun fetchListOnline(offset: Int, quantity: Int = 10): List<Pokemon> {
        try {
            val fetchPokemonList = pokemonAPI.fetchPokemonList(offset, quantity)
            return fetchPokemonList.results.map {
                val id = it.url
                    .replace(PokemonAPI.BASE_URL, "")
                    .replace("pokemon", "")
                    .replace("/", "")
                Pokemon(id.toLong(), it.name, offset)
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
                    fetchPokemonOnline(it.id)
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

    fun fetchPokemonAllDetailsLocal(pokemonId: Long) = liveData {
        emitSource(pokemonDAO.fetchPokemonById(pokemonId))
        withContext(Dispatchers.IO) {
            mutableListOf<Deferred<Any?>>().apply {
                add(async {
                    fetchPokemonOnline(pokemonId)?.let { pokemon ->
                        pokemonDAO.insertPokemon(pokemon)
                    }
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
                    if (it.stat?.name != null) {
                        val name = it.stat.name.replace("-", " ").capitalize()
                        Stat(name, it.baseStat ?: 0L, it.effort ?: 0L)
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

                val abilities = fetchPokemonById.abilities
                    .mapNotNull {
                        it.ability?.name?.replace("-", " ")?.capitalize()
                    }
                    .distinct()

                val pokemon = Pokemon(
                    pokemonId,
                    directPokemon.name,
                    directPokemon.offset,
                    abilities,
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
                directPokemon
            }
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

                val flavorTextEntries = fetchSpeciePokemonById.flavorTextEntries
                    ?.filter { it.language?.name == "en" }
                    ?.mapNotNull { it.flavorText }
                    ?.map { it.replace("[\n\\f]+".toRegex(), " ") }
                    ?.distinctBy { it }

                return PokemonSpecie(
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

                return encounterAreaPokemon.mapNotNull {
                    it.locationArea?.name?.let { nameArea ->
                        it.versionDetails
                            ?.firstOrNull()?.encounterDetails?.firstOrNull()
                            ?.let { encounterDetail ->
                                val name = nameArea.replace("-", " ").capitalize()
                                PokemonArea(
                                    name,
                                    encounterDetail.chance,
                                    encounterDetail.minLevel,
                                    encounterDetail.maxLevel
                                )
                            }
                    }
                }
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