package com.example.pokedex.data.remote

import com.example.pokedex.core.capitalize
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.data.remote.model.PokemonDetailedResponse

internal object ConverterRemote {
    fun sprites(fetchPokemonByName: PokemonDetailedResponse) =
        mutableListOf<String>().apply {
            fetchPokemonByName.sprites?.other?.let { other ->
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
        }.toList()

    fun stats(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.stats
            .mapNotNull {
                if (it.stat?.name != null) {
                    val name = it.stat.name.replace("-", " ").capitalize()
                    Stat(name, it.baseStat ?: 0L, it.effort ?: 0L)
                } else {
                    null
                }
            }

    fun types(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.types
            .mapNotNull { it.type?.name }
            .distinct()
            .sorted()

    fun moves(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.moves
            .mapNotNull {
                it.move?.name?.replace("-", " ")?.capitalize()
            }
            .distinct()
            .sorted()

    fun abilities(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.abilities
            .mapNotNull {
                it.ability?.name?.replace("-", " ")?.capitalize()
            }
            .distinct()
            .sorted()
}