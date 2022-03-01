package com.example.pokedex.data

import android.util.Log
import androidx.lifecycle.liveData
import com.example.pokedex.core.EventSource
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

    fun pokemonPaging() = pokemonDAO.fetchPokemonsPaging()

    suspend fun insert(pokemon: List<Pokemon>) = pokemonDAO.insertPokemons(pokemon)

    fun fetchListPokemonLocal(offset: Int = 0, quantity: Int = 10) = liveData {
        emitSource(pokemonDAO.getAllPokemon())

        val hasPokemon = pokemonDAO.hasPokemon(offset)
        if (!hasPokemon) {
            val pokemonList = fetchListOnline(offset, quantity)
            pokemonDAO.insertPokemons(pokemonList)

            val pokemonWithDetails = fetchPokemonDetails(pokemonList)
            pokemonDAO.insertPokemons(pokemonWithDetails)
        }
    }

    fun fetchFavoritePokemonLocal() = liveData {
        emitSource(pokemonDAO.getAllFavoritePokemon())
    }

    suspend fun hasPokemon(offset: Int) = pokemonDAO.hasPokemon(offset)

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
            val mapAsync = pokemonList.map { async { fetchPokemonOnline(it.id) } }
            mapAsync.awaitAll().filterNotNull()
        }
    }

    suspend fun fetchPokemonDirect(pokemonId: Long): Pokemon? {
        return try {
            pokemonDAO.fetchDirectPokemonById(pokemonId)
        } catch (e: Exception) {
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
            if (directPokemon.pokemonSpecie == null && directPokemon.specie?.url?.isNotEmpty() == true) {
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
        } catch (e: Exception) {
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
                pokemonDAO.updatePokemonArea(pokemonId, areas)
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineEncounterArea")
        }
    }

    suspend fun savePokemonInFavorites(pokemonId: Long, isFavorite: Boolean) {
        pokemonDAO.favoritePokemon(pokemonId, isFavorite)
    }

    fun sendPokemonInfo(pokemonId: Long) = liveData<EventSource<Boolean>?> {
        emit(EventSource.Loading("Looking for Pokemon information"))
        try {
            pokemonDAO.fetchDirectPokemonById(pokemonId).apply {
                emit(EventSource.Loading("Sending information from ${this.name}"))
                pokemonAPI.sendPokemonInfo(this)
                emit(EventSource.Ready(true, "Pokemon info sent"))
            }
        } catch (e: Exception) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineEncounterArea")
            emit(EventSource.Error("Error fetching or sending Pokemon information"))
        }
    }

}