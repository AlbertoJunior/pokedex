package com.example.pokedex.data

import android.util.Log
import androidx.lifecycle.liveData
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.data.local.room.PokemonDAO
import com.example.pokedex.data.remote.PokemonAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
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

    suspend fun fetchListOnline(offset: Int, quantity: Int) {
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
            val listAsync = listOf(
                async {
                    fetchPokemonOnline(pokemonId)?.let {
                        pokemonDAO.insertPokemon(it)
                    }
                },
                async {
                    if (allDetails) {
                        fetchPokemonOnlineAllDetails(pokemonId)
                    }
                })
            listAsync.awaitAll()
        }
    }

    private suspend fun fetchPokemonOnline(pokemonId: Long, insert: Boolean = false): Pokemon? {
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

                val pokemon = Pokemon(
                    pokemonId,
                    directPokemon.name,
                    directPokemon.offset,
                    fetchPokemonById.abilities,
                    fetchPokemonById.moves,
                    fetchPokemonById.height,
                    fetchPokemonById.locationAreaEncounters,
                    fetchPokemonById.baseExperience,
                    fetchPokemonById.species,
                    fetchPokemonById.stats.mapNotNull {
                        return@mapNotNull if (it.stat?.name != null) {
                            Stat(it.stat.name, it.baseStat ?: 0L, it.effort ?: 0L)
                        } else {
                            null
                        }
                    },
                    listSprites,
                    fetchPokemonById.types,
                    fetchPokemonById.weight
                )

                if (insert)
                    pokemonDAO.insertPokemon(pokemon)

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
            if (directPokemon.pokemonSpecie == null) {
                val fetchSpeciePokemonById = pokemonAPI.fetchSpeciePokemonById(pokemonId)

                val flavorTextEntries = fetchSpeciePokemonById.flavorTextEntries
                    ?.filter { it.language?.name == "en" }
                    ?.distinctBy { it.flavorText }
                    ?.mapNotNull { it.flavorText }
                    ?.map { it.replace("\n", " ").replace("\\f", " ") }

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
                directPokemon.pokemonSpecie = pokemonSpecie
                pokemonDAO.insertPokemon(directPokemon)
            }
        } catch (e: SQLException) {
            Log.e("PokemonRepository", e.message ?: "fetchPokemonOnlineAllDetails")
        }
    }

    suspend fun savePokemonInFavorites(pokemonId: Long, isFavorite: Boolean) {
        pokemonDAO.favoritePokemon(pokemonId, isFavorite)
    }

}