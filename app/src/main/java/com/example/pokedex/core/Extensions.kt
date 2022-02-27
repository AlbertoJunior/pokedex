package com.example.pokedex.core

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.pokedex.R

fun String.capitalize(): String {
    val camelRegex = "[a-z]|( [a-z])".toRegex()
    return this.replaceFirst(camelRegex, this.first().uppercase())
}

@BindingAdapter("colorByText")
fun View.colorByText(value: String?) {
    val color = when (value) {
        "yellow" -> R.color.yellow_200
        "red" -> R.color.red_700
        "green" -> R.color.green_700
        "blue" -> R.color.blue_550
        "black" -> R.color.black_700
        "white" -> R.color.white_700
        else -> R.color.blue_700
    }
    background.setTint(ContextCompat.getColor(context, color))
}

@BindingAdapter("textColorByText")
fun TextView.textColorByText(value: String?) {
    val color = when (value) {
        "yellow" -> R.color.yellow_200
        "red" -> R.color.red_700
        "green" -> R.color.green_700
        "blue" -> R.color.blue_550
        "black" -> R.color.black_700
        "white" -> R.color.white_700
        else -> R.color.blue_700
    }
    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, color)))
}
