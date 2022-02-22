package com.example.pokedex.data

import androidx.lifecycle.liveData
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.room.PokemonDAO
import com.example.pokedex.data.remote.PokemonAPI
import javax.inject.Inject

class PokemonRepository @Inject constructor(
    private val pokemonAPI: PokemonAPI,
    private val pokemonDAO: PokemonDAO
) {

    fun fetchListPokemon(offset: Int = 0, quantity: Int = 10) = liveData {
        emitSource(pokemonDAO.fetchPokemons(offset, quantity))
    }

    suspend fun fetchOnline(offset: Int, quantity: Int): List<Pokemon> {
        val hasPokemon = pokemonDAO.hasPokemons(offset)
        if (!hasPokemon) {
            val fetchPokemonList = pokemonAPI.fetchPokemonList(offset, quantity)
            val map = fetchPokemonList.results.map {
                val id = it.url
                    .replace(PokemonAPI.BASE_URL, "")
                    .replace("pokemon", "")
                    .replace("/", "")
                Pokemon(id.toLong(), it.name, offset)
            }
            pokemonDAO.insertPokemons(map)
            return map
        }
        return emptyList()
    }

    fun fetchPokemon(pokemonId: Long) = liveData {
        val source = pokemonDAO.fetchPokemonById(pokemonId)
        emitSource(source)

        val directPokemon = pokemonDAO.fetchDirectPokemonById(pokemonId)
        if (!directPokemon.isComplete()) {
            val fetchPokemonById = pokemonAPI.fetchPokemonById(pokemonId)
            println(fetchPokemonById)
        }
    }
}