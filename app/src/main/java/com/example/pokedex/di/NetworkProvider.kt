package com.example.pokedex.di

import com.example.pokedex.data.remote.PokemonAPI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvider {

    @Provides
    @Singleton
    fun retrofitProvider(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(PokemonAPI.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun retrofitPokemonApiProvider(client: Retrofit): PokemonAPI =
        client.create(PokemonAPI::class.java)

    @Provides
    @Singleton
    fun okHttpClientProvider() = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}