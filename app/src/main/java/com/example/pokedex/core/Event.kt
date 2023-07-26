package com.example.pokedex.core

import androidx.lifecycle.Observer

open class Event<out T>(private val content: T) {
    private var hasBeenHandled = false
    fun getIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peek(): T = content
}

class EventObserver<T>(private val onEventHandled: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
       value.getIfNotHandled()?.let {
           onEventHandled(it)
       }
    }
}