package com.example.pokedex.data.remote.model

import com.google.gson.annotations.SerializedName

data class PokemonDetailedResponse(
    val abilities: List<Ability> = emptyList(),
    @SerializedName(value = "baseExperience", alternate = ["base_experience"])
    val baseExperience: Long? = null,
    val forms: List<NameUrlObject> = emptyList(),
    val height: Long? = null,
    val id: Long? = null,
    @SerializedName(value = "locationAreaEncounters", alternate = ["location_area_encounters"])
    val locationAreaEncounters: String? = null,
    val moves: List<Move> = emptyList(),
    val name: String? = null,
    val species: NameUrlObject? = null,
    val sprites: Sprites? = null,
    val stats: List<StatResponse> = emptyList(),
    val types: List<Type> = emptyList(),
    val weight: Long? = null
)

data class Ability(
    val ability: NameUrlObject? = null,
    @SerializedName(value = "isHidden", alternate = ["is_hidden"])
    val isHidden: Boolean? = null,
)

data class Move(
    val move: NameUrlObject? = null
)

data class Sprites(
    val other: Other? = null
)

data class Other(
    @SerializedName(value = "dreamWorld", alternate = ["dream_world"])
    val dreamWorld: FrontDefault? = null,
    val home: FrontDefault? = null,
    @SerializedName(value = "officialArtwork", alternate = ["official_artwork", "official-artwork"])
    val officialArtwork: FrontDefault? = null
)

data class FrontDefault(
    @SerializedName(value = "frontDefault", alternate = ["front_default"])
    val frontDefault: String? = null
)

data class StatResponse(
    @SerializedName(alternate = ["baseStat"], value = "base_stat")
    val baseStat: Long? = null,
    val effort: Long? = null,
    val stat: NameUrlObject? = null
)

data class Type(
    val type: NameUrlObject? = null
)

