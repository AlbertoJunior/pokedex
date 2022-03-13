package com.example.pokedex.data.remote.model

import com.google.gson.annotations.SerializedName

data class PokemonGenerationResponse(
    val id: Long,
    @SerializedName(value = "mainRegion", alternate = ["main_region"])
    val mainRegion: NameUrlObject? = null,
    val moves: List<NameUrlObject>? = null,
    val name: String? = null,
    @SerializedName(value = "pokemonSpecies", alternate = ["pokemon_species"])
    val pokemonSpecies: List<NameUrlObject>? = null,
    val types: List<NameUrlObject>? = null,
    @SerializedName(value = "versionGroups", alternate = ["version_groups"])
    val versionGroups: List<NameUrlObject>? = null
)