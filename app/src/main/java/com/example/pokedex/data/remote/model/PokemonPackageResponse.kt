package com.example.pokedex.data.remote.model

data class PokemonPackageResponse (
    val count: Long,
    val next: String,
    val previous: String? = null,
    val results: List<PokemonResponse>
)