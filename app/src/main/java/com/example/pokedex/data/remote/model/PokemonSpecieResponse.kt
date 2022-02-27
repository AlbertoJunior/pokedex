package com.example.pokedex.data.remote.model

import com.google.gson.annotations.SerializedName

data class PokemonSpecieResponse(
    @SerializedName(value = "baseHappiness", alternate = ["base_happiness"])
    val baseHappiness: Long? = null,
    @SerializedName(value = "captureRate", alternate = ["capture_rate"])
    val captureRate: Long? = null,
    val color: SimplyObject? = null,
    @SerializedName(value = "eggGroups", alternate = ["egg_groups"])
    val eggGroups: List<SimplyObject>? = null,
    @SerializedName(value = "evolutionChain", alternate = ["evolution_chain"])
    val evolutionChain: EvolutionChain? = null,
    @SerializedName(value = "flavorTextEntries", alternate = ["flavor_text_entries"])
    val flavorTextEntries: List<FlavorTextEntry>? = null,
    @SerializedName(value = "growthRate", alternate = ["growth_rate"])
    val growthRate: SimplyObject? = null,
    val habitat: SimplyObject? = null,
    @SerializedName(value = "isBaby", alternate = ["is_baby"])
    val isBaby: Boolean? = null,
    @SerializedName(value = "isLegendary", alternate = ["is_legendary"])
    val isLegendary: Boolean? = null,
    @SerializedName(value = "isMythical", alternate = ["is_mythical"])
    val isMythical: Boolean? = null,
    val shape: SimplyObject? = null
)

data class EvolutionChain(
    val url: String? = null
)

data class FlavorTextEntry(
    @SerializedName(value = "flavorText", alternate = ["flavor_text"])
    val flavorText: String? = null,
    val language: SimplyObject? = null,
    val version: SimplyObject? = null
)