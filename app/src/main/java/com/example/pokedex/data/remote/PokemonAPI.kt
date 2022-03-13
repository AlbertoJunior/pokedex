package com.example.pokedex.data.remote

import com.example.pokedex.data.local.model.Pokemon
import com.example.pokedex.data.remote.model.*
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

    @GET("generation/{generation}/")
    suspend fun fetchPokemonByGeneration(@Path("generation") generation: Int): PokemonGenerationResponse

    @POST(WEBHOOK)
    suspend fun sendPokemonInfo(@Body directPokemon: Pokemon)
}