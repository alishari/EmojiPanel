package com.momt.emojipanel

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

interface DrawableProvider {
    companion object {
        fun of(resId: Int) = object : DrawableProvider {
            override fun getIconResource(): Int = resId
        }

        fun of(drawable: Drawable?) = object : DrawableProvider {
            override fun getIconDrawable(): Drawable? = drawable
        }
    }

    fun getIconResource(): Int = 0
    fun getIconDrawable(): Drawable? = null

    fun getDrawable(context: Context): Drawable? =
        if (getIconResource() != 0)
            ContextCompat.getDrawable(context, getIconResource())
        else
            getIconDrawable()
}