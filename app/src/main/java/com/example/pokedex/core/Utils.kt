package com.example.pokedex.core

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.pokedex.R

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
                .apply {
                    placeHolder?.let { placeholder(it).into(into) } ?: into(into)
                }
        }

        /**
         * @param fade -> true fade in, false fade out
         */
        fun fade(
            fade: Boolean,
            vararg objects: View,
            duration: Long = 400,
            delay: Long = 0,
            listenerOnAnimationStart: () -> Unit = {},
            listenerOnAnimationEnd: () -> Unit = {},
        ) {
            objects.firstOrNull()?.context?.let { context ->
                val anim = if (fade) R.anim.fade_in else R.anim.fade_out
                val animation = AnimationUtils.loadAnimation(context, anim)
                animation.duration = duration
                animation.startOffset = delay
                val value = object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {
                        objects.forEach { it.isVisible = !fade }
                        listenerOnAnimationStart.invoke()
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        objects.forEach { it.isVisible = fade }
                        listenerOnAnimationEnd.invoke()
                    }

                    override fun onAnimationRepeat(p0: Animation?) {}
                }
                animation.setAnimationListener(value)

                objects.forEach { item ->
                    item.animation = animation
                }
                animation.start()
            }
        }
    }

}