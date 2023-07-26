package com.example.pokedex.view.search.viewmodel

import androidx.lifecycle.*
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.local.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonSearchViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _pokemonId = MutableLiveData<String?>(null)
    private val pokemonId: LiveData<String?> = _pokemonId

    val pokemon: LiveData<EventSource<Pokemon?>> = pokemonId.switchMap { value ->
        if (value.isNullOrBlank())
            return@switchMap liveData { EventSource.Ready<Pokemon?>(null) }

        try {
            pokemonRepository.searchPokemonByIdAllDetailsLocal(value.toLong())
        } catch (e: NumberFormatException) {
            val preparedName = value.lowercase().replace(" ", "-")
            pokemonRepository.searchPokemonByNameAllDetailsLocal(preparedName)
        }
    }

    fun setPokemonId(value: String?) {
        _pokemonId.value = value
    }
}