package com.example.pokedex.core

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.example.pokedex.R
import com.google.android.material.progressindicator.CircularProgressIndicator

fun String.capitalize(): String {
    return this.split(" ")
        .joinToString(" ") { it.replaceFirst("[a-z]".toRegex(), it.first().uppercase()) }
}

@BindingAdapter("visibilityByBoolean")
fun View.visibilityByBoolean(value: Boolean?) {
    this.isVisible = value ?: false
}

@BindingAdapter("colorByText")
fun View.colorByText(value: String?) {
    val color = getColor(value)

    when (this) {
        is ImageView -> {
            imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
        }
        is CircularProgressIndicator -> {
            setIndicatorColor(ContextCompat.getColor(context, color))
        }
        else -> {
            background.setTint(ContextCompat.getColor(context, color))
        }
    }
}

private fun getColor(value: String?): Int {
    val color = when (value) {
        "yellow" -> R.color.yellow_200
        "red" -> R.color.red_700
        "green" -> R.color.green_700
        "blue" -> R.color.blue_550
        "black" -> R.color.black_700
        "white" -> R.color.white_700
        "brown" -> R.color.brown_500
        "gray" -> R.color.gray_500
        "pink" -> R.color.pink_500
        "purple" -> R.color.purple_500
        else -> R.color.blue_700
    }
    return color
}

@BindingAdapter("textColorByText")
fun TextView.textColorByText(value: String?) {
    setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, getColor(value))))
}

