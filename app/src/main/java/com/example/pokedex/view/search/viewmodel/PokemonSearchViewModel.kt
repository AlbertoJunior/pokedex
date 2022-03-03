package com.example.pokedex.view.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.pokedex.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonSearchViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _pokemonId = MutableLiveData<String?>(null)
    private val pokemonId: LiveData<String?> = _pokemonId

    val pokemon = Transformations.switchMap(pokemonId) { value ->
        if (value == null)
            return@switchMap null

        try {
            pokemonRepository.fetchPokemonAllDetailsLocal(value.toLong())
        } catch (e: Exception) {
            val preparedName = value.lowercase().replace(" ", "-")
            pokemonRepository.fetchPokemonByNameAllDetailsLocal(preparedName)
        }
    }

    fun setPokemonId(value: String?) {
        _pokemonId.value = value
    }
}