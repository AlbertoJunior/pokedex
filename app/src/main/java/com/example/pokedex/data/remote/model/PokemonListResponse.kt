package com.example.pokedex.data.remote.model

data class PokemonListResponse(
    val count: Long,
    val next: String?,
    val previous: String? = null,
    val results: List<PokemonResponse>
)