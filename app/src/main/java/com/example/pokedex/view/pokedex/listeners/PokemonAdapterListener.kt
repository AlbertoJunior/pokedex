package com.example.pokedex.view.pokedex.listeners

import com.example.pokedex.data.local.model.Pokemon

interface PokemonAdapterListener {
    fun onPokemonClicked(pokemon: Pokemon)
}