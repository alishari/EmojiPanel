package com.momt.emojipanel.utils

import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.View
import com.google.android.material.tabs.TabLayout

fun <T> Iterator<T>.toIterable() = object : Iterable<T> {
    override fun iterator(): Iterator<T> = this@toIterable
}

fun View.getRectOnScreen(): Rect {
    val loc = IntArray(2)
    getLocationOnScreen(loc)
    return Rect(loc[0], loc[1], loc[0] + width, loc[1] + height)
}

fun TabLayout.setAccentColor(color: Int) {
    setSelectedTabIndicatorColor(color)
    tabRippleColor = makeTabRippleAccentColor(tabRippleColor, color)
    tabIconTint = makeTabIconTintColor(tabIconTint, color)
}

private fun makeTabRippleAccentColor(colorList: ColorStateList?, newAccentColor: Int): ColorStateList? {
    if (colorList == null) return null

    val colors = ColorStateList::class.java.getDeclaredField("mColors").apply { isAccessible = true }
        .run { get(colorList) as IntArray }
    @Suppress("UNCHECKED_CAST")
    val states = ColorStateList::class.java.getDeclaredField("mStateSpecs").apply { isAccessible = true }
        .run { get(colorList) as Array<IntArray> }
    val currentRawColor = colors[
            states
                .filter(IntArray::isNotEmpty)
                .indexOfFirst { arr -> arr.contains(android.R.attr.state_selected) }] and
            0x00FFFFFF
    val newRawColor = newAccentColor and 0x00FFFFFF     //Removing alpha
    return ColorStateList(
        states,
        colors.map { color ->
            if (color and 0x00FFFFFF == currentRawColor) newRawColor or (color and (0xFF shl 24))   //Adding alpha
            else color
        }.toIntArray()
    )
}

private fun makeTabIconTintColor(colorList: ColorStateList?, newAccentColor: Int): ColorStateList? {
    return makeTabRippleAccentColor(colorList, newAccentColor)  //Currently the same thing
}