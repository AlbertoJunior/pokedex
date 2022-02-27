package com.example.pokedex.core

sealed class EventSource<out T> {
    class Loading<T>(val message: String? = "Loading") : EventSource<T>()

    class Error<T>(val message: String? = "Error", val detailedError: String? = null) : EventSource<T>()

    class Ready<T>(val value: T, val message: String? = "") : EventSource<T>()
}