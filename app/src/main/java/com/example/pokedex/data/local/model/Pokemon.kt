package com.example.pokedex.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pokedex.core.capitalize
import com.example.pokedex.data.remote.model.NameUrlObject

@Entity
data class Pokemon(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val name: String,
    val offset: Int,
    val abilities: List<String>? = null,
    val moves: List<String>? = null,
    val height: Long? = null,
    val locationAreaEncounters: String? = null,
    val baseExperience: Long? = null,
    val specie: NameUrlObject? = null,
    val stats: List<Stat>? = null,
    val sprites: List<String>? = null,
    val types: List<String> = emptyList(),
    val weight: Long? = null,
    val favorite: Boolean = false,
    val pokemonSpecie: PokemonSpecie? = null,
    val pokemonArea: List<PokemonArea> = emptyList()
) {

    fun getNameShow() = name.capitalize()

    fun getFavoriteText() = if (favorite) "Release!" else "Catch!"

    fun getImage() = sprites?.firstOrNull { url ->
        url.isNotEmpty() && url.isNotBlank() && !url.endsWith(".svg")
    } ?: ""

    fun getImageOfficial() = sprites?.firstOrNull { url ->
        url.isNotEmpty() && url.isNotBlank() && !url.endsWith(".svg") && url.contains("official")
    } ?: ""

    fun getWeightConverted() = weight?.div(10F) ?: 0.0F

    fun getHeightConverted() = height?.div(10F) ?: 0.0F

    fun isComplete(): Boolean {
        return sprites != null && types.isNotEmpty() && moves != null && abilities != null && stats != null
    }

}