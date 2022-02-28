package com.example.pokedex.data.local.model

import com.example.pokedex.core.capitalize

data class PokemonSpecie(
    val baseHappiness: Long? = null,
    val captureRate: Long? = null,
    val color: String? = null,
    val flavorTextEntries: List<String>? = null,
    val growthRate: String? = null,
    val habitat: String? = null,
    val isBaby: Boolean? = null,
    val isLegendary: Boolean? = null,
    val isMythical: Boolean? = null,
    val shape: String? = null
) {
    fun getHabitatShow() = habitat?.replace("-", " ")?.capitalize() ?: ""

    fun getShapeShow() = shape?.replace("-", " ")?.capitalize() ?: ""
}