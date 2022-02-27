package com.example.pokedex.core

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class Utils {
    companion object {
        fun loadImageGlide(
            context: Context,
            urlImg: String,
            into: ImageView,
            placeHolder: Int? = null,
            listenerOnReady: () -> Unit = {},
            listenerOnError: () -> Unit = {}
        ) {
            Glide.with(context)
                .load(urlImg)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("loadImageGlide", e?.message ?: "onLoadFailed")
                        listenerOnError.invoke()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        listenerOnReady.invoke()
                        return false
                    }

                })
                .fitCenter()
                .circleCrop()
                .apply {
                    placeHolder?.let { placeholder(it).into(into) } ?: into(into)
                }
        }
    }

}