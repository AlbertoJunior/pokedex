package com.example.pokedex.view.pokedex.viewmodel

import androidx.lifecycle.*
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.local.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(private val repository: PokemonRepository) :
    ViewModel() {

    private var lastOffset = 0

    fun fetchPokemonList(
        offset: Int = 0,
        quantity: Int = 10
    ): LiveData<EventSource<List<Pokemon>>> {
        return repository.fetchListPokemon(offset, quantity).map {
            if (it.isNotEmpty())
                EventSource.Ready(it)
            else
                EventSource.Error("No one pokemon has reached")
        }
    }

    fun fetchOnline(
        offset: Int = lastOffset,
        quantity: Int = 10
    ) {
        viewModelScope.launch {
            val fetchOnline = repository.fetchOnline(offset, quantity)
            lastOffset += fetchOnline.size
        }
    }
}