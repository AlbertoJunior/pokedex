package com.example.pokedex.data.local.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pokedex.core.capitalize
import com.example.pokedex.data.remote.model.*

@Entity
data class Pokemon(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val name: String,
    val offset: Int,
    val abilities: List<Ability>? = null,
    val moves: List<Move>? = null,
    val height: Long? = null,
    val locationAreaEncounters: String? = null,
    val baseExperience: Long? = null,
    val species: NameUrlObject? = null,
    val stats: List<Stat>? = null,
    val sprites: List<String>? = null,
    val types: List<Type>? = null,
    val weight: Long? = null,
    val favorite: Boolean = false,
    @Embedded
    var pokemonSpecie: PokemonSpecie? = null
) {

    fun getNameShow() = name.capitalize()

    fun getImage() = sprites?.firstOrNull { url ->
        url.isNotEmpty() && url.isNotBlank() && !url.endsWith(".svg")
    } ?: ""

    fun isComplete(): Boolean {
        return sprites != null && types != null && moves != null && abilities != null && stats != null
    }

}