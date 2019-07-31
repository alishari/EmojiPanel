package com.momt.emojipanel.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.momt.emojipanel.R
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

    val thePager: ViewPager by lazy { containerView.findViewById<ViewPager>(R.id.vp) }

    val theTabs: TabLayout by lazy { containerView.findViewById<TabLayout>(R.id.tabs) }

    private val items: MutableList<Fragment> = mutableListOf()

    private val backspacePageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

        override fun onPageSelected(position: Int) {
            val item = adapter.getItem(position)
            val showBackspace: Boolean
            var backspacable: BackspaceSupporter? = null
            if (item is BackspaceSupporter) {
                showBackspace = true
                backspacable = item
            } else if (item is ViewFragment && item.v is BackspaceSupporter) {
                showBackspace = true
                backspacable = item.v
            } else
                showBackspace = false

            if (showBackspace)
                containerView.findViewById<ImageView>(R.id.btn_right).let {
                    it.setImageResource(R.drawable.ic_round_backspace_24dp)
                    it.visibility = View.VISIBLE
                    it.setOnClickListener { backspacable!!.onBackspacePressed() }
                }
            else
                containerView.findViewById<View>(R.id.btn_right).visibility = View.INVISIBLE
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

        thePager.addOnPageChangeListener(backspacePageChangeListener)
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

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child == containerView)
            super.addView(child, index, params)
        else if (child != null)
            addItem(child, index = index)
    }

    /**
     * Adds a view as page to the viewpager
     * @param child The view to be converted to a fragment and added as a page
     * @param iconProvider The icon provider of this page (shown in the bottom tab)
     * @param index Position of the new page (at the end by default)
     */
    fun addItem(child: View, iconProvider: PageIconProvider? = child as? PageIconProvider, index: Int = -1) {
        addItem(ViewFragment(child), iconProvider, index)
    }

    /**
     * Adds a fragment as page the viewpager
     * @param item The fragment to be added as a page
     * @param iconProvider The icon provider of this page (shown in the bottom tab)
     * @param index Position of the new page (at the end by default)
     */
    fun addItem(item: Fragment, iconProvider: PageIconProvider? = null, index: Int = -1) {
        theTabs.addTab(tabs.newTab().apply {
            if (iconProvider == null) return@apply
            if (iconProvider.getIconResource() != 0)
                setIcon(iconProvider.getIconResource())
            else
                setIcon(iconProvider.getIconDrawable())
        }, if (index < 0) theTabs.tabCount else index)

        items.add(if (index < 0) items.size else index, item)
        adapter.notifyDataSetChanged()
        backspacePageChangeListener.onPageSelected(thePager.currentItem)

        if (item is ScrollReplicator)
            item.setTargetCoordinatorLayout(findViewById(R.id.container))
        else if (item is ViewFragment && item.v is ScrollReplicator)
            item.v.setTargetCoordinatorLayout(findViewById(R.id.container))
    }

    fun getItemAt(index: Int) = items[index]

    fun selectPage(index: Int, smoothScroll: Boolean = false) = thePager.setCurrentItem(index, smoothScroll)

    /**
     * Use this method instead of [findViewById] if you want to find a view
     */
    fun <T : View> getItemById(id: Int) = items.map { it.view ?: (it as? ViewFragment)?.v }.find { it?.id == id } as T

    private inner class MyAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment = items[position]

        override fun getCount(): Int = items.size

        override fun getItemPosition(`object`: Any): Int {
            if (`object` !is Fragment)
                return PagerAdapter.POSITION_NONE
            val index = items.indexOf(`object`)
            return if (index >= 0) index else PagerAdapter.POSITION_NONE
        }

    }

    /**
     * If your page has an icon should implement this interface. Used to show an icon in the tabs bellow
     */
    interface PageIconProvider {
        companion object {
            fun of(resId: Int) = object : PageIconProvider {
                override fun getIconResource(): Int = resId
            }

            fun of(drawable: Drawable?) = object : PageIconProvider {
                override fun getIconDrawable(): Drawable? = drawable
            }
        }

        fun getIconResource(): Int = 0
        fun getIconDrawable(): Drawable? = null
    }

    /**
     * If your page can handle and needs backspace button should implement this interface.
     */
    interface BackspaceSupporter {
        fun onBackspacePressed()
    }

    /**
     * If your page has a nested scroll view should implement this and send scroll events to the [CoordinatorLayout].
     * Used for bottom tabs visibility change
     */
    interface ScrollReplicator {
        fun setTargetCoordinatorLayout(target: CoordinatorLayout)
    }

    class ViewFragment(val v: View) : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            v
    }
}