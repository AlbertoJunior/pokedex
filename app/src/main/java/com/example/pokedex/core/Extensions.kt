package com.example.pokedex.core

import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.example.pokedex.R
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.progressindicator.CircularProgressIndicator

fun String.capitalize(): String {
    if (this.isBlank() || this.isEmpty())
        return ""
    return this.split(" ")
        .joinToString(" ") { it.replaceFirst("[a-z]".toRegex(), it.first().uppercase()) }
}

@BindingAdapter("visibilityByBoolean")
fun View.visibilityByBoolean(value: Boolean?) {
    this.isVisible = value ?: false
}

@BindingAdapter("colorByText")
fun View.colorByText(value: String?) {
    getColor(value)?.let {
        val color = ContextCompat.getColor(context, it)

        when (this) {
            is ImageView -> imageTintList = ColorStateList.valueOf(color)
            is CircularProgressIndicator -> setIndicatorColor(color)
            is MaterialDivider -> dividerColor = color
            is TextView -> getColorText(value)?.let { colorText ->
                setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, colorText)))
            }
            else -> background.setTint(color)
        }
    }
}

@BindingAdapter("colorByTextContrast")
fun View.colorByTextContrast(value: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        when (value) {
            "black", "white" -> R.color.gray_700
            else -> {
                if (resources.configuration.isNightModeActive)
                    androidx.appcompat.R.color.background_floating_material_dark
                else
                    androidx.appcompat.R.color.background_floating_material_light
            }
        }.let {
            val color = ContextCompat.getColor(context, it)

            when (this) {
                is ImageView -> imageTintList = ColorStateList.valueOf(color)
                is CircularProgressIndicator -> setIndicatorColor(color)
                else -> setBackgroundColor(color)
            }
        }
    }
}

fun getColor(value: String?): Int? {
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
        else -> null
    }
    return color
}

fun getColorText(value: String?): Int? {
    return when (value) {
        "yellow" -> R.color.yellow_700
        "red" -> R.color.red_700
        "green" -> R.color.green_700
        "blue" -> R.color.blue_550
        "black" -> R.color.black_700
        "white" -> R.color.white_700
        "brown" -> R.color.brown_500
        "gray" -> R.color.gray_500
        "pink" -> R.color.pink_500
        "purple" -> R.color.purple_500
        else -> null
    }
}

@BindingAdapter("textColorByText")
fun TextView.textColorByText(value: String?) {
    getColorText(value)?.let { color ->
        setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, color)))
    }
}
