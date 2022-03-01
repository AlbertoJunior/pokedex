package com.example.pokedex.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.pokedex.data.local.model.Pokemon

@ExperimentalPagingApi
class PokemonRemotePagingSource(
    private val pokemonRepository: PokemonRepository,
) : RemoteMediator<Int, Pokemon>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Pokemon>
    ): MediatorResult {

        val offset = when (loadType) {
            LoadType.REFRESH -> {
                state.anchorPosition?.let { anchorPosition ->
                    state.closestItemToPosition(anchorPosition)?.offset
                } ?: 0
            }
            LoadType.PREPEND -> state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.offset?.minus(
                10
            ) ?: -1
            LoadType.APPEND -> state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.offset?.plus(
                10
            ) ?: -1
        }

        return try {
            val response = pokemonRepository.fetchListOnline(offset, state.config.pageSize)
            pokemonRepository.insert(response)
            val fetchPokemonDetails = pokemonRepository.fetchPokemonDetails(response)
            pokemonRepository.insert(fetchPokemonDetails)

            return MediatorResult.Success(fetchPokemonDetails.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        if (pokemonRepository.hasPokemon(0)) {
            return InitializeAction.SKIP_INITIAL_REFRESH
        }
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}