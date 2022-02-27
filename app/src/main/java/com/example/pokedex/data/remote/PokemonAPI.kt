package com.example.pokedex.data.remote

import com.example.pokedex.data.remote.model.PokemonDetailedResponse
import com.example.pokedex.data.remote.model.PokemonListResponse
import com.example.pokedex.data.remote.model.PokemonSpecieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonAPI {
    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }

    @GET("pokemon")
    suspend fun fetchPokemonList(@Query("offset") offset: Int, @Query("limit") quantity: Int): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun fetchPokemonById(@Path("id") id: Long): PokemonDetailedResponse

    @GET("pokemon-species/{id}")
    suspend fun fetchSpeciePokemonById(@Path("id") id: Long): PokemonSpecieResponse

    @GET("pokemon/{name}")
    suspend fun fetchPokemonByName(@Path("name") name: String): PokemonDetailedResponse
}