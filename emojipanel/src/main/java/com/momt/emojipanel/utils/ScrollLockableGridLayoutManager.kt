package com.momt.emojipanel.utils

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

/**
 * A [GridLayoutManager] which makes it possible to disable the scrolls
 */
class ScrollLockableGridLayoutManager(context: Context, spansCount: Int) : GridLayoutManager(context, spansCount) {

    var canScrollVertically = true

    override fun canScrollVertically(): Boolean =
        if (!canScrollVertically) false
        else super.canScrollVertically()

    var canScrollHorizontally = true

    override fun canScrollHorizontally(): Boolean =
        if (!canScrollHorizontally) false
        else super.canScrollHorizontally()
}