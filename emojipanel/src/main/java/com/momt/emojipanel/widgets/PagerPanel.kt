package com.momt.emojipanel.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.momt.emojipanel.DrawableProvider
import com.momt.emojipanel.R
import com.momt.emojipanel.utils.hasFlag
import com.momt.emojipanel.utils.makeTabIconTintDefaultColor
import com.momt.emojipanel.utils.setAccentColor
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.design_pagerpanel.view.*

/**
 * A [ViewPager] which provides tabs, backspaces, ...
 * @property thePager the [ViewPager] inside
 * @property theTabs the [TabLayout] which is shown at the bottom
 */
class PagerPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LayoutContainer {

    override val containerView: View = View.inflate(context, R.layout.design_pagerpanel, null)

    private val thePager: ViewPager by lazy { containerView.findViewById<ViewPager>(R.id.vp) }

    val theTabs: TabLayout by lazy { containerView.findViewById<TabLayout>(R.id.tabs) }

    val rightButton: ImageView by lazy { containerView.findViewById<ImageView>(R.id.btn_right) }

    val leftButton: ImageView by lazy { containerView.findViewById<ImageView>(R.id.btn_left) }

    private val items: MutableList<Fragment> = mutableListOf()

    private val bottomButtonsPageChangeListener = object : ViewPager.OnPageChangeListener {
        private val holdClickTriggerTouchListener by lazy { ButtonHoldClickTriggerTouchListener() }

        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        @SuppressLint("ClickableViewAccessibility")
        override fun onPageSelected(position: Int) {
            val item = adapter.getItem(position)

            val buttonOwner: BottomButtonOwner
            if (item is BottomButtonOwner)
                buttonOwner = item
            else if (item is ViewFragment && item.child is BottomButtonOwner)
                buttonOwner = item.child as BottomButtonOwner
            else {
                leftButton.visibility = View.INVISIBLE
                rightButton.visibility = View.INVISIBLE
                return
            }

            leftButton.setOnClickListener { buttonOwner.onBottomButtonClick(BottomButtonOwner.LEFT) }
            rightButton.setOnClickListener { buttonOwner.onBottomButtonClick(BottomButtonOwner.RIGHT) }

            val icons = buttonOwner.getBottomButtonIcons()
            leftButton.setImageDrawable(icons.first?.getDrawable(context))
            rightButton.setImageDrawable(icons.second?.getDrawable(context))

            val types = buttonOwner.getBottomButtonsTypes()
            leftButton.visibility = if (types.hasFlag(BottomButtonOwner.LEFT)) View.VISIBLE else View.INVISIBLE
            rightButton.visibility = if (types.hasFlag(BottomButtonOwner.RIGHT)) View.VISIBLE else View.INVISIBLE

            val triggerTypes = buttonOwner.getClickTriggerOnHoldButtonsTypes()
            leftButton.setOnTouchListener(if (types.hasFlag(BottomButtonOwner.LEFT)) holdClickTriggerTouchListener else null)
            rightButton.setOnTouchListener(if (types.hasFlag(BottomButtonOwner.RIGHT)) holdClickTriggerTouchListener else null)
        }
    }

    private val tabsPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) {
            theTabs.getTabAt(position)?.select()
        }
    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(p0: TabLayout.Tab?) = Unit

        override fun onTabUnselected(p0: TabLayout.Tab?) = Unit

        override fun onTabSelected(tab: TabLayout.Tab?) {
            selectPage(tab?.position ?: 0, true)
        }

    }

    init {
        addView(
            containerView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        if (context is AppCompatActivity)
            initWith(context)

        thePager.addOnPageChangeListener(bottomButtonsPageChangeListener)
        thePager.addOnPageChangeListener(tabsPageChangeListener)

        theTabs.addOnTabSelectedListener(tabSelectedListener)
    }

    private lateinit var adapter: MyAdapter

    private var activity: AppCompatActivity? = null
    /**
     * Sets the activity to be initialized with. It uses its [FragmentManager]
     */
    fun initWith(activity: AppCompatActivity) {
        adapter = MyAdapter(activity.supportFragmentManager)
        thePager.adapter = adapter
        this.activity = activity
    }

    //region Items management

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child == containerView)
            super.addView(child, index, params)
        else if (child != null)
            addItem(child, index = index)
    }

    /**
     * Adds a child as page to the viewpager
     * @param child The child to be converted to a fragment and added as a page
     * @param iconProvider The icon provider of this page (shown in the bottom tab)
     * @param index Position of the new page (at the end by default)
     */
    fun addItem(child: View, iconProvider: DrawableProvider? = child as? DrawableProvider, index: Int = -1) {
        addItem(ViewFragment().apply { this.child = child }, iconProvider, index)
    }

    /**
     * Adds a fragment as page the viewpager
     * @param item The fragment to be added as a page
     * @param iconProvider The icon provider of this page (shown in the bottom tab)
     * @param index Position of the new page (at the end by default)
     */
    fun addItem(item: Fragment, iconProvider: DrawableProvider? = null, index: Int = -1) {
        theTabs.addTab(tabs.newTab().apply {
            if (iconProvider == null) return@apply
            icon = iconProvider.getDrawable(context)
        }, if (index < 0) theTabs.tabCount else index)

        items.add(if (index < 0) items.size else index, item)
        adapter.notifyDataSetChanged()
        bottomButtonsPageChangeListener.onPageSelected(thePager.currentItem)

        if (item is ScrollReplicator)
            item.setTargetCoordinatorLayout(findViewById(R.id.container))
        else if (item is ViewFragment && item.child is ScrollReplicator)
            (item.child as ScrollReplicator).setTargetCoordinatorLayout(findViewById(R.id.container))
    }

    fun getItemAt(index: Int) = items[index]

    fun selectPage(index: Int, smoothScroll: Boolean = false) = thePager.setCurrentItem(index, smoothScroll)

    /**
     * Use this method instead of [findViewById] if you want to find a child
     */
    fun <T : View> getItemById(id: Int) =
        items.map { it.view ?: (it as? ViewFragment)?.child }.find { it?.id == id } as T

    private inner class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = items[position]

        override fun getCount(): Int = items.size

        override fun getItemPosition(`object`: Any): Int {
            if (`object` !is Fragment)
                return PagerAdapter.POSITION_NONE
            val index = items.indexOf(`object`)
            return if (index >= 0) index else PagerAdapter.POSITION_NONE
        }

        override fun getItemId(position: Int): Long {       // preventing fragment recreations
            return System.currentTimeMillis()
        }
    }

    class ViewFragment : Fragment() {
        var child: View? = null
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            child


    }

    //endregion

    /**
     * If your page handle and or needs bottom buttons should implement this interface.
     */
    interface BottomButtonOwner {
        companion object {
            @Retention(AnnotationRetention.SOURCE)
            @IntDef(NONE, LEFT, RIGHT, BOTH)
            annotation class ButtonType

            const val NONE = 0
            const val LEFT = 1
            const val RIGHT = 2
            const val BOTH = 3
        }

        @ButtonType
        fun getBottomButtonsTypes(): Int

        fun onBottomButtonClick(@ButtonType buttonId: Int)
        /**
         * @return A pair which [Pair.first] represents left button's icon and [Pair.second] represents right button's icon
         */
        fun getBottomButtonIcons(): Pair<DrawableProvider?, DrawableProvider?>

        @ButtonType
        fun getClickTriggerOnHoldButtonsTypes(): Int
    }

    /**
     * If your page can handle and needs backspace button should implement this interface.
     */
    interface BackspaceSupporter : BottomButtonOwner {
        override fun getBottomButtonsTypes(): Int = BottomButtonOwner.RIGHT

        override fun getBottomButtonIcons(): Pair<DrawableProvider?, DrawableProvider?> =
            Pair(null, DrawableProvider.of(R.drawable.ic_round_backspace_24dp))

        override fun getClickTriggerOnHoldButtonsTypes(): Int = BottomButtonOwner.RIGHT

        override fun onBottomButtonClick(buttonId: Int) {
            if (buttonId.hasFlag(BottomButtonOwner.RIGHT))
                onBackspacePressed()
        }

        fun onBackspacePressed()
    }

    /**
     * If your page has a nested scroll view should implement this and send scroll events to the [CoordinatorLayout].
     * Used for bottom tabs visibility change
     */
    interface ScrollReplicator {
        fun setTargetCoordinatorLayout(target: CoordinatorLayout)
    }

    //region Theme

    fun setSelectedTabColor(color: Int) {
        theTabs.setAccentColor(color)
    }

    /**
     * Sets default/unselected tab icon color
     */
    fun setButtonAndDefaultTabColor(color: Int) {
        theTabs.tabIconTint = makeTabIconTintDefaultColor(theTabs.tabIconTint, color)
        leftButton.setColorFilter(color)
        rightButton.setColorFilter(color)
    }

    fun setBottomBarBackgroundColor(color: Int) {
        findViewById<View>(R.id.bottom_bar).setBackgroundColor(color)
        theTabs.setBackgroundColor(color)
    }

    //endregion
}