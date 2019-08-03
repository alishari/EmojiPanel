package com.momt.emojipanel.widgets

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.tabs.TabLayout

/**
 * A [TabLayout.OnTabSelectedListener] which when a tab is selected calls the layoutManagers's smooth scroll to
 * the header item based on [headerPositions]
 * @property scrollListener The scroll listener to be disabled to prevent loops
 * @property disableForScrollSet Used to disable the functionality when we are setting tabs based on scroll
 */
internal class EmojiTabLayoutTabSelectedListener(
    private val headerPositions: ArrayList<Int>,
    private val layoutManager: LinearLayoutManager,
    private val context: Context
) :
    TabLayout.OnTabSelectedListener {
    var disableForScrollSet = false

    var scrollListener: EmojiListTabSelectorScrollListener? = null

    //Simply sets the layout manager to scroll till item is at top of the list
    private class SnapToStartLinearSmoothScroller(context: Context) : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference() = SNAP_TO_START
    }

    override fun onTabReselected(tab: TabLayout.Tab?) = onTabSelected(tab)

    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (disableForScrollSet)
            return
        scrollListener?.disabledForProgrammaticallyScroll = true
        val smoothScroller = SnapToStartLinearSmoothScroller(context)
        smoothScroller.targetPosition = headerPositions[tab?.position ?: 0]
        layoutManager.startSmoothScroll(smoothScroller)
    }

}