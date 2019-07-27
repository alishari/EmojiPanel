package com.momt.emojipanel

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

/**
 * A [RecyclerView.OnScrollListener] which syncs the tabs above the list with the current category based on [headerPositions]
 * @param layoutManager Used to find the first visible item in the list
 * @param tabLayout The target [TabLayout] which tabs will be selected
 * @property disabledForProgrammaticallyScroll Used to disable functionality when we are scrolling using tabs
 * @property tabSelectedListener The [TabLayout.OnTabSelectedListener] to be disabled to prevent loops
 */
internal class EmojiListTabSelectorScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val headerPositions: ArrayList<Int>,
    private val tabLayout: TabLayout
) : RecyclerView.OnScrollListener() {

    var disabledForProgrammaticallyScroll = false

    var tabSelectedListener: EmojiTabLayoutTabSelectedListener? = null

    private var lastCategory: Int = -1

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (disabledForProgrammaticallyScroll)
            return
        val index = layoutManager.findFirstVisibleItemPosition()
        val category = headerPositions.withIndex().findLast { it.value <= index }?.index ?: 0
        if (lastCategory != category) {
            tabSelectedListener?.disableForScrollSet = true
            tabLayout.getTabAt(category)?.select()
            tabSelectedListener?.disableForScrollSet = false
            lastCategory = category
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (disabledForProgrammaticallyScroll && newState == RecyclerView.SCROLL_STATE_IDLE)
            disabledForProgrammaticallyScroll = false
    }
}