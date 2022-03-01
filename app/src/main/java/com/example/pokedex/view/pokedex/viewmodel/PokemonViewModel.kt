package com.example.pokedex.view.pokedex.viewmodel

import android.view.View
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.pokedex.core.EventSource
import com.example.pokedex.data.PokemonRemotePagingSource
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.data.local.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _loadingStatus = MutableLiveData<Int>()
    val loadingStatus: LiveData<Int> = _loadingStatus

    private val _bottomNavigationStatus = MutableLiveData<Int>()
    val bottomNavigationStatus: LiveData<Int> = _bottomNavigationStatus

    @OptIn(ExperimentalPagingApi::class)
    val pokemonPaging = Pager(
        PagingConfig(10, initialLoadSize = 10, enablePlaceholders = true),
        remoteMediator = PokemonRemotePagingSource(repository)
    ) {
        repository.pokemonPaging()
    }
        .flow
        .cachedIn(viewModelScope)

    fun fetchFavoritePokemonList(): LiveData<EventSource<List<Pokemon>>> {
        showLoading()
        return Transformations.switchMap(repository.fetchFavoritePokemonLocal()) {
            hideLoading()
            val value: EventSource<List<Pokemon>> = if (it.isNotEmpty()) {
                EventSource.Ready(it)
            } else {
                EventSource.Error("You don't have any favorite pokemon :(")
            }
            return@switchMap MutableLiveData(value)
        }
    }

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

}