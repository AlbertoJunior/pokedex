package com.example.pokedex.view.pokemon.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.core.EventSource
import com.example.pokedex.core.capitalize
import com.example.pokedex.data.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(private val repository: PokemonRepository) :
    ViewModel() {

    private val _favoriteEvent = MutableLiveData<EventSource<Boolean>?>()
    val favoriteEvent: LiveData<EventSource<Boolean>?> = _favoriteEvent

    fun fetchPokemonUltraDetail(id: Long) = repository.fetchPokemonLocal(id, true)

    fun savePokemonFavorite(pokemonId: Long) {
        viewModelScope.launch {
            _favoriteEvent.value = EventSource.Loading("Trying catch")

            val fetchPokemonDirect = repository.fetchPokemonDirect(pokemonId)
            if (fetchPokemonDirect != null) {
                val swapFavoriteStatus = !fetchPokemonDirect.favorite
                repository.savePokemonInFavorites(pokemonId, swapFavoriteStatus)

                val message = if (swapFavoriteStatus)
                    "Gotcha! ${fetchPokemonDirect.name.capitalize()} was caught!"
                else
                    "You released ${fetchPokemonDirect.name.capitalize()}"

                _favoriteEvent.value = EventSource.Ready(swapFavoriteStatus, message)
            } else {
                _favoriteEvent.value = EventSource.Error("The Pokemon broke free!")
            }
        }
    }

    fun clearFavoriteEvent() {
        _favoriteEvent.value = null
    }

    fun sendPokemonInfo(pokemonId: Long) = repository.sendPokemonInfo(pokemonId)
}