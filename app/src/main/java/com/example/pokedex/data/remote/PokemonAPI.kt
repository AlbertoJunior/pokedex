package com.example.pokedex.data.remote

import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.remote.model.LocationAreaEncounterElement
import com.example.pokedex.data.remote.model.PokemonDetailedResponse
import com.example.pokedex.data.remote.model.PokemonListResponse
import com.example.pokedex.data.remote.model.PokemonSpecieResponse
import retrofit2.http.*

interface PokemonAPI {
    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"

        // manager https://webhook.site/#!/ec6aa581-f5f6-4ceb-979c-b22071aa4fa1
        const val WEBHOOK = "https://webhook.site/ec6aa581-f5f6-4ceb-979c-b22071aa4fa1"
    }

    @GET("pokemon")
    suspend fun fetchPokemonList(
        @Query("offset") offset: Int,
        @Query("limit") quantity: Int
    ): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun fetchPokemonById(@Path("id") id: Long): PokemonDetailedResponse

    @GET("pokemon/{name}")
    suspend fun fetchPokemonByName(@Path("name") name: String): PokemonDetailedResponse

    @GET("pokemon-species/{id}")
    suspend fun fetchSpeciePokemonById(@Path("id") id: Long): PokemonSpecieResponse

    @GET("pokemon/{id}/encounters")
    suspend fun fetchEncounterAreaPokemonById(@Path("id") id: Long): Array<LocationAreaEncounterElement>

    @POST(WEBHOOK)
    suspend fun sendPokemonInfo(@Body directPokemon: Pokemon)
}