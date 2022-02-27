package com.example.pokedex.core

sealed class EventSource<out T>(val message: String?) {
    class Loading<T>(message: String? = "Loading") : EventSource<T>(message)

    class Error<T>(message: String? = "Error") : EventSource<T>(message)

    class Ready<T>(val value: T, message: String? = "") : EventSource<T>(message)
}