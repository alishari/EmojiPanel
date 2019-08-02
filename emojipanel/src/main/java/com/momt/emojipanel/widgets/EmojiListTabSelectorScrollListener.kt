package com.momt.emojipanel.widgets

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

    private var lastCategory: Int = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (disabledForProgrammaticallyScroll)
            return

        val firstIndex = layoutManager.findFirstVisibleItemPosition()
        val category =
            if (lastCategory + 1 < headerPositions.size && firstIndex >= headerPositions[lastCategory + 1]) {
                //Finding the last one which matches firstIndex >= headerPosition.
                //Because probability of facing it from this is more we start from lastCategory + 1
                //Kotlin collections dropWhile has performance problems.
                var result = headerPositions.size - 1
                for (i in lastCategory + 1 until headerPositions.size)
                    if (firstIndex < headerPositions[i]) {
                        result = i - 1
                        break
                    }
                result
            } else if (firstIndex < headerPositions[lastCategory])
                headerPositions.subList(0, lastCategory).indexOfLast { it <= firstIndex }
            else
                lastCategory

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