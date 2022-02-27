package com.example.pokedex.view.pokedex.viewmodel

import android.view.View
import androidx.lifecycle.*
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.local.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(private val repository: PokemonRepository) :
    ViewModel() {

    private val _loadingStatus = MutableLiveData<Int>()
    val loadingStatus = _loadingStatus

    private val _bottomNavigationStatus = MutableLiveData<Int>()
    val bottomNavigationStatus = _bottomNavigationStatus

    fun fetchPokemonList(): LiveData<EventSource<List<Pokemon>>> {
        showLoading()
        return Transformations.switchMap(repository.fetchListPokemonLocal()) {
            hideLoading()
            val value: EventSource<List<Pokemon>> = if (it.isNotEmpty()) {
                EventSource.Ready(it)
            } else {
                EventSource.Error("No one pokemon has reached")
            }
            return@switchMap MutableLiveData(value)
        }
    }

    fun fetchFavoritePokemonList(): LiveData<EventSource<List<Pokemon>>> {
        showLoading()
        return Transformations.switchMap(repository.fetchFavoritePokemonsLocal()) {
            hideLoading()
            val value: EventSource<List<Pokemon>> = if (it.isNotEmpty()) {
                EventSource.Ready(it)
            } else {
                EventSource.Error("You don't have any favorite pokemon :(")
            }
            return@switchMap MutableLiveData(value)
        }
    }

    fun fetchPokemonUltraDetail(id: Long) = repository.fetchPokemonLocal(id, true)

    private fun showLoading() {
        _loadingStatus.value = View.VISIBLE
    }

    private fun hideLoading() {
        _loadingStatus.value = View.GONE
    }

    fun showBottomNav() {
        _bottomNavigationStatus.value = View.VISIBLE
    }

    fun hideBottomNav() {
        _bottomNavigationStatus.value = View.GONE
    }

    fun savePokemonFavorite(pokemonId: Long) {
        viewModelScope.launch {
            val fetchPokemonDirect = repository.fetchPokemonDirect(pokemonId)
            if (fetchPokemonDirect != null) {
                repository.savePokemonInFavorites(pokemonId, !fetchPokemonDirect.favorite)
            } else {
                //SHOW ERROR
            }
        }
    }

}