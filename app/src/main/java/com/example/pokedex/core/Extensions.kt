package com.example.pokedex.core

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.pokedex.R

fun String.capitalize(): String {
    return this.split(" ")
        .joinToString(" ") { it.replaceFirst("[a-z]".toRegex(), it.first().uppercase()) }
}

@BindingAdapter("visibilityByBoolean")
fun View.visibilityByBoolean(value: Boolean) {
    this.visibility = if (value) View.VISIBLE else View.GONE
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

        "brown" -> R.color.white_700
        "gray" -> R.color.white_700
        "pink" -> R.color.white_700
        "purple" -> R.color.white_700
        else -> R.color.blue_700
    }

    if (this is ImageView) {
        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
    } else {
        background.setTint(ContextCompat.getColor(context, color))
    }
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

