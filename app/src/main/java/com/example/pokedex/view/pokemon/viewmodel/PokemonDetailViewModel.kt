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
import retrofit2.HttpException
import java.sql.SQLException
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(private val repository: PokemonRepository) :
    ViewModel() {

    private val _favoriteEvent = MutableLiveData<EventSource<Boolean>?>()
    val favoriteEvent: LiveData<EventSource<Boolean>?> = _favoriteEvent

    private val _sendInfoEvent = MutableLiveData<EventSource<Boolean>?>()
    val sendInfoEvent: LiveData<EventSource<Boolean>?> = _sendInfoEvent

    fun fetchPokemonDetail(id: Long) = repository.fetchPokemonAllDetailsLocal(id)

    fun savePokemonFavorite(pokemonId: Long) {
        viewModelScope.launch {
            _favoriteEvent.value = EventSource.Loading("Trying catch")

            val fetchPokemonDirect = repository.safeFetchPokemonDirect(pokemonId)
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

    fun sendPokemonInfo(pokemonId: Long) {
        _sendInfoEvent.value = EventSource.Loading("Looking for Pokemon information")
        viewModelScope.launch {
            try {
                val pokemon = repository.sendPokemonInfo(pokemonId)
                _sendInfoEvent.value =
                    EventSource.Ready(true, "Pokemon ${pokemon.name.capitalize()} info sent")
            } catch (e: SQLException) {
                _sendInfoEvent.value =
                    EventSource.Error("Oh no! You probably lost the pokeball")
            } catch (e: HttpException) {
                _sendInfoEvent.value = EventSource.Error("Failed to send Pokemon information")
            } catch (e: Exception) {
                _sendInfoEvent.value = EventSource.Error(e.message)
            }
        }
    }

    fun clearSendEvent() {
        _sendInfoEvent.value = null
    }
}