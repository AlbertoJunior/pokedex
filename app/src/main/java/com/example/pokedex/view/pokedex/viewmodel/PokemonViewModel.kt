package com.example.pokedex.view.pokedex.viewmodel

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.local.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _pokemonId = MutableLiveData<Long?>()
    val pokemonId: LiveData<Long?> = _pokemonId

    private val _bottomNavigationStatus = MutableLiveData(true)
    val bottomNavigationStatus: LiveData<Boolean> = _bottomNavigationStatus

    private val _popBackStack = MutableLiveData<Boolean?>(null)
    val popBackStackEvent: LiveData<Boolean?> = _popBackStack

    var showingLottie = true
        private set

    var lastPokemonId: Long? = null
        private set

    val pokemonListAll = repository.pokemonListAllPaging.cachedIn(viewModelScope)

    fun fetchFavoritePokemonList(): LiveData<EventSource<List<Pokemon>>> {
        return Transformations.switchMap(repository.fetchFavoritePokemonLocal()) {
            val value: EventSource<List<Pokemon>> = if (it.isNotEmpty()) {
                EventSource.Ready(it)
            } else {
                EventSource.Error("You don't have any favorite pokemon :(")
            }
            return@switchMap MutableLiveData(value)
        }
    }

    fun setPokemonId(pokemonId: Long?) {
        _pokemonId.value = pokemonId
        lastPokemonId = pokemonId
    }

    fun showBottomNav() {
        _bottomNavigationStatus.value = true
    }

    fun hideBottomNav() {
        _bottomNavigationStatus.value = false
    }

    fun setLottieVisibility(showing: Boolean) {
        showingLottie = showing
    }

    fun popBackStackEvent(pop: Boolean?) {
        _popBackStack.value = pop
    }

}