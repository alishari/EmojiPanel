package com.momt.emojipanel.utils

import android.graphics.Rect
import android.view.View

fun <T> Iterator<T>.toIterable() = object : Iterable<T> {
    override fun iterator(): Iterator<T> = this@toIterable
}

fun View.getRectOnScreen(): Rect {
    val loc = IntArray(2)
    getLocationOnScreen(loc)
    return Rect(loc[0], loc[1], loc[0] + width, loc[1] + height)
}