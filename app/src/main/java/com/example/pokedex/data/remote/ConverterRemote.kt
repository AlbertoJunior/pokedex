package com.example.pokedex.data.remote

import com.example.pokedex.core.capitalize
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.local.model.PokemonArea
import com.example.pokedex.data.local.model.PokemonSpecie
import com.example.pokedex.data.local.model.Stat
import com.example.pokedex.data.remote.model.LocationAreaEncounterElement
import com.example.pokedex.data.remote.model.PokemonDetailedResponse
import com.example.pokedex.data.remote.model.PokemonResponse
import com.example.pokedex.data.remote.model.PokemonSpecieResponse
import kotlin.math.max

internal object ConverterRemote {

    fun id(pokemonResponse: PokemonResponse) =
        pokemonResponse.url
            .replace(PokemonAPI.BASE_URL, "")
            .replace("pokemon", "")
            .replace("/", "")
            .toLong()

    fun name(name: String?) =
        name?.replace("-", " ")?.capitalize() ?: ""

    fun pokemon(fetchPokemonById: PokemonDetailedResponse): Pokemon {
        val name = name(fetchPokemonById.name)
        val listSprites = sprites(fetchPokemonById)
        val abilities = abilities(fetchPokemonById)
        val moves = moves(fetchPokemonById)
        val stats = stats(fetchPokemonById)
        val types = types(fetchPokemonById)
        val offset = offset(fetchPokemonById)

        return Pokemon(
            fetchPokemonById.id ?: throw Exception("Pokemon ID Can't be null"),
            name,
            offset,
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
    }

    private fun offset(fetchPokemonByName: PokemonDetailedResponse): Int {
        val auxId = fetchPokemonByName.id ?: throw Exception("ID is NULL")
        return max((auxId - (auxId % 10) - 10), 0L).toInt()
    }

    private fun sprites(fetchPokemonByName: PokemonDetailedResponse) =
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

    private fun stats(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.stats
            .mapNotNull {
                if (it.stat?.name != null) {
                    val name = it.stat.name.replace("-", " ").capitalize()
                    Stat(name, it.baseStat ?: 0L, it.effort ?: 0L)
                } else {
                    null
                }
            }

    private fun types(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.types
            .mapNotNull { it.type?.name?.capitalize() }
            .distinct()
            .sorted()

    private fun moves(fetchPokemonByName: PokemonDetailedResponse) =
        fetchPokemonByName.moves
            .mapNotNull {
                it.move?.name?.replace("-", " ")?.capitalize()
            }
            .distinct()
            .sorted()

    private fun abilities(pokemonResponse: PokemonDetailedResponse) =
        pokemonResponse.abilities
            .mapNotNull {
                it.ability?.name?.replace("-", " ")?.capitalize()
            }
            .distinct()
            .sorted()

    fun pokemonArea(encounterAreaPokemon: Array<LocationAreaEncounterElement>?) =
        encounterAreaPokemon?.mapNotNull {
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
        } ?: emptyList()

    private fun flavorText(pokemonSpecieResponse: PokemonSpecieResponse) =
        pokemonSpecieResponse.flavorTextEntries
            ?.filter { it.language?.name == "en" }
            ?.mapNotNull { it.flavorText }
            ?.map { it.replace("[\n\\f]+".toRegex(), " ") }
            ?.distinctBy { it }

    fun pokemonSpecie(pokemonSpecieResponse: PokemonSpecieResponse) =
        PokemonSpecie(
            pokemonSpecieResponse.baseHappiness,
            pokemonSpecieResponse.captureRate,
            pokemonSpecieResponse.color?.name,
            flavorText(pokemonSpecieResponse),
            pokemonSpecieResponse.growthRate?.name,
            name(pokemonSpecieResponse.habitat?.name),
            pokemonSpecieResponse.isBaby,
            pokemonSpecieResponse.isLegendary,
            pokemonSpecieResponse.isMythical,
            name(pokemonSpecieResponse.shape?.name)
        )
}