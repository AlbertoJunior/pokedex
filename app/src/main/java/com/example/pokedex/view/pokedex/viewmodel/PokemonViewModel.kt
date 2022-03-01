package com.example.pokedex.view.pokedex.viewmodel

import android.view.View
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.paging.PokemonRemotePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _pokemonId = MutableLiveData<Long?>()
    val pokemonId: LiveData<Long?> = _pokemonId

    private val _bottomNavigationStatus = MutableLiveData<Int>()
    val bottomNavigationStatus: LiveData<Int> = _bottomNavigationStatus

    @OptIn(ExperimentalPagingApi::class)
    val pokemonListAllPaging = Pager(
        PagingConfig(10, initialLoadSize = 10, enablePlaceholders = true),
        remoteMediator = PokemonRemotePagingSource(repository)
    ) {
        repository.pokemonListAllPaging()
    }
        .flow
        .cachedIn(viewModelScope)

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
    }

    fun showBottomNav() {
        _bottomNavigationStatus.value = View.VISIBLE
    }

    fun hideBottomNav() {
        _bottomNavigationStatus.value = View.GONE
    }

}