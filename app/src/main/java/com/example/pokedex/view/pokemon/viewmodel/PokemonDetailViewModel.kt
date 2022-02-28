package com.example.pokedex.view.pokemon.viewmodel

import android.view.View
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

    private val _loadingStatus = MutableLiveData<Int>()
    val loadingStatus: LiveData<Int> = _loadingStatus

    private val _favoriteEvent = MutableLiveData<EventSource<Boolean>?>()
    val favoriteEvent: LiveData<EventSource<Boolean>?> = _favoriteEvent

    fun fetchPokemonUltraDetail(id: Long) = repository.fetchPokemonLocal(id, true)

    private fun showLoading() {
        _loadingStatus.value = View.VISIBLE
    }

    private fun hideLoading() {
        _loadingStatus.value = View.GONE
    }

    fun savePokemonFavorite(pokemonId: Long) {
        showLoading()
        viewModelScope.launch {
            val fetchPokemonDirect = repository.fetchPokemonDirect(pokemonId)
            if (fetchPokemonDirect != null) {
                val swapFavoriteStatus = !fetchPokemonDirect.favorite
                repository.savePokemonInFavorites(pokemonId, swapFavoriteStatus)

                val message = if (swapFavoriteStatus)
                    "Gotcha! ${fetchPokemonDirect.name.capitalize()} was caught!"
                else
                    "You released ${fetchPokemonDirect.name.capitalize()}"

                _favoriteEvent.value = EventSource.Ready(swapFavoriteStatus, message)
                hideLoading()
            } else {
                _favoriteEvent.value = EventSource.Error("The Pokemon broke free!")
                hideLoading()
            }
        }
    }

    fun clearFavoriteEvent() {
        _favoriteEvent.value = null
    }

    fun sendPokemonInfo(pokemonId: Long) {
        viewModelScope.launch {
            showLoading()
            if (repository.sendPokemonInfo(pokemonId) == 1) {
                println("terminou")
            } else {
                println("Erro")
            }
            hideLoading()
        }
    }

}