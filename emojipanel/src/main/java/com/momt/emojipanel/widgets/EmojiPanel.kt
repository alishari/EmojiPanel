package com.momt.emojipanel.widgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.edit
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.momt.emojipanel.*
import com.momt.emojipanel.adapters.EmojiListAdapter
import com.momt.emojipanel.adapters.ItemClickEventArgs
import com.momt.emojipanel.emoji.EmojiData
import com.momt.emojipanel.emoji.EmojiUtils
import com.momt.emojipanel.utils.ScrollLockableGridLayoutManager
import com.momt.emojipanel.utils.toIterable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.design_emojipanel.view.*

/**
 * The emoji panel which has the list and tabs bound with it
 * @property txtBoundWith The text field which emojis will be inserted into
 */
class EmojiPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LayoutContainer,
    PagerPanel.PageIconProvider, PagerPanel.BackspaceSupporter, PagerPanel.ScrollReplicator {
    companion object {
        private const val DEFAULT_MAX_RECENT_EMOJI = 20

        private const val EMOJIS_USAGE_STATISTICS_PREFERENCE_NAME = "emojisUsage"
        private const val EMOJIS_SKIN_COLORS_PREFERENCE_NAME = "skinColors"

        private val CATEGORY_COUNT = EmojiData.dataColored.size + 1
    }

    private var usageStatistics = HashMap<String, Int>()
    private var defaultSkinColor = ""
    private var skinColors = HashMap<String, String>()

    var txtBoundWith: EditText? = null

    override val containerView: View = View.inflate(context, R.layout.design_emojipanel, null)

    val theList: RecyclerView by lazy { containerView.findViewById<RecyclerView>(R.id.list) }

    val theTabs: TabLayout by lazy { containerView.findViewById<TabLayout>(R.id.tabs) }

    init {
        ApplicationLoader.setContext(context)
        AndroidUtilities.density = context.resources.displayMetrics.density
        EmojiUtils.loadAllEmojis()

        addView(
            containerView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        //Do not change this initialization order
        context.obtainStyledAttributes(
            attrs,
            R.styleable.EmojiPanel + intArrayOf(R.array.def_categories_icons, R.array.def_categories_titles)
        )
            .apply(this::initIconsAndHeaders)
            .also(TypedArray::recycle)

        loadSettings()
        initList()
        initTabs()
    }

    //region Initialization


    private lateinit var categoriesIcons: IntArray
    private lateinit var categoriesTitles: Array<String>

    private fun initIconsAndHeaders(ta: TypedArray) {
        var iconsId = ta.getResourceId(R.styleable.EmojiPanel_categoriesIcons, 0)
        if (iconsId == 0) iconsId = R.array.def_categories_icons
        resources.obtainTypedArray(iconsId)
            .apply { categoriesIcons = IntArray(CATEGORY_COUNT) { i -> getResourceId(i, 0) } }
            .also(TypedArray::recycle)

        var titlesId = ta.getResourceId(R.styleable.EmojiPanel_categoriesTitles, 0)
        if (titlesId == 0) titlesId = R.array.def_categories_titles
        resources.obtainTypedArray(titlesId)
            .apply { categoriesTitles = Array(CATEGORY_COUNT) { i -> getString(i) ?: "" } }
            .also(TypedArray::recycle)
    }


    private lateinit var headerPositions: ArrayList<Int>

    private lateinit var adapter: EmojiListAdapter
    private lateinit var layoutManager: ScrollLockableGridLayoutManager
    private lateinit var listScrollListener: EmojiListTabSelectorScrollListener

    private val touchLongClickListener = ReplicatorTouchListener()

    private fun initList() {

        adapter = EmojiListAdapter(
            context,
            makeEmojisList(),
            categoriesTitles,
            defaultSkinColor,
            skinColors,
            usageStatistics.isNotEmpty()
        ).apply {
            itemClicked += this@EmojiPanel::listItemClicked
            itemLongClicked += this@EmojiPanel::listItemLongClicked
        }
        theList.adapter = adapter

        layoutManager = ScrollLockableGridLayoutManager(context, 8)
            .apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int =
                        when (adapter.getItemViewType(position)) {
                            EmojiListAdapter.EMOJI_VIEW_TYPE -> 1
                            EmojiListAdapter.HEADER_VIEW_TYPE -> this@apply.spanCount
                            else -> 0
                        }
                }
            }
        theList.layoutManager = layoutManager

        listScrollListener = EmojiListTabSelectorScrollListener(layoutManager, headerPositions, tabs)
        theList.addOnScrollListener(listScrollListener)
    }

    private fun makeEmojisList(): MutableList<String> {
        headerPositions = ArrayList(EmojiData.dataColored.size + 1)
        var lastHeaderPosition = 0
        return (sequenceOf(findRecentEmojis().toTypedArray()) + EmojiData.dataColored)
            .withIndex()
            .flatMap {
                if (it.value.isEmpty()) return@flatMap emptySequence<String>()
                headerPositions.add(lastHeaderPosition)
                lastHeaderPosition += it.value.size + 1
                return@flatMap (sequenceOf("H" + it.index.toString()) + Sequence { it.value.iterator() })
            }.iterator().toIterable().toMutableList()
    }

    private fun findRecentEmojis(): List<String> =
        usageStatistics.entries
            .sortedByDescending { it.value }
            .take(DEFAULT_MAX_RECENT_EMOJI)
            .map { it.key }

    private fun listItemClicked(sender: EmojiListAdapter, args: ItemClickEventArgs) =
        emojiClicked(adapter.items[args.position], (args.view as EmojiImageView).emojiCode)

    private fun listItemLongClicked(sender: EmojiListAdapter, args: ItemClickEventArgs) {
        layoutManager.canScrollVertically = false
        layoutManager.canScrollHorizontally = false
        args.view.setOnTouchListener(touchLongClickListener)
        EmojiColorPopupWindow(context, adapter.items[args.position])
            .apply {
                itemClicked += { _, innerArgs ->
                    emojiClickedFromSkinColorPopup(
                        innerArgs.selectedCode,
                        args.position
                    )
                }
                dismissed += { _, _ ->
                    layoutManager.canScrollVertically = true
                    layoutManager.canScrollHorizontally = true

                    args.view.setOnTouchListener(null)
                }
            }
            .also { touchLongClickListener.target = it.contentView }
            .showAsDropDown(
                args.view,
                0,
                -(EmojiColorPopupWindow.getPopupHeight(context) + args.view.height)
            )
    }

    private fun emojiClicked(raw: String, code: String = raw) {
        insertTextInto(txtBoundWith, code)
        increaseUsageStatistics(raw)
    }

    private fun emojiClickedFromSkinColorPopup(code: String, adapterPosition: Int) {
        insertTextInto(txtBoundWith, code)
        val (raw, color) = EmojiUtils.extractRawAndColorFromCode(code)
        increaseUsageStatistics(raw)
        if (color.isEmpty()) //No color
            skinColors.remove(raw)
        else
            skinColors[raw] = color
        adapter.notifyItemChanged(adapterPosition)
    }

    private fun insertTextInto(txt: EditText?, str: String) {
        if (txt == null)
            return
        txt.text.insert(txt.selectionStart, str)
    }

    private fun increaseUsageStatistics(rawCode: String) {
        usageStatistics[rawCode] = (usageStatistics[rawCode] ?: 0) + 1
    }

    private fun initTabs() {
        categoriesIcons.forEach {
            theTabs.addTab(theTabs.newTab().apply {
                setIcon(it)
                view.setPadding(0)  //avoiding possible icon crop
            })
        }

        if (usageStatistics.isEmpty())
            theTabs.removeTabAt(0)

        theTabs.addOnTabSelectedListener(EmojiTabLayoutTabSelectedListener(headerPositions, layoutManager, context)
            .apply
            {
                this.scrollListener = listScrollListener
                listScrollListener.tabSelectedListener = this
            })
    }

    /**
     * Updates recent emoji part of the list based on the usage statistics map
     */
    fun updateRecentEmojis() {
        if (headerPositions.isEmpty()) return
        if (adapter.hasRecent) {
            val currentRecents = adapter.items.subList(1, headerPositions[1])
            val previousCount = currentRecents.size
            currentRecents.clear()
            currentRecents.addAll(findRecentEmojis())
            //It's impossible to have reduced count
            adapter.notifyItemRangeChanged(1, previousCount)
            adapter.notifyItemRangeInserted(previousCount + 1, currentRecents.size - previousCount)
            val shiftAmount = currentRecents.size - previousCount
            for (i in 1 until headerPositions.size)
                headerPositions[i] += shiftAmount
        } else {
            val recents = findRecentEmojis()
            if (recents.isEmpty()) return
            adapter.items.add(0, "H0")
            adapter.items.addAll(1, recents)
            tabs.addTab(tabs.newTab().apply {
                setIcon(R.drawable.ic_round_recent_24dp)
                view.setPadding(0)  //avoiding possible icon crop
            }, 0)
            adapter.notifyItemRangeInserted(0, recents.size + 1)
            adapter.hasRecent = true
            headerPositions.add(0, 0)
            for (i in 1 until headerPositions.size)
                headerPositions[i] += recents.size + 1
        }

    }

    //endregion

    //region Preferences and Settings

    /**
     * Reads recent emojis and skin colors from preferences
     */
    fun loadSettings() {
        loadEmojisUsageStatistics()
        loadEmojisSkinColors()
    }

    /**
     * Saves recent emojis and skin colors to preferences
     * @param commit Sets whether to commit or apply the preference changes
     */
    fun saveSettings(commit: Boolean = false) {
        saveEmojisUsageStatistics(commit)
        saveEmojisSkinColors(commit)
    }

    private fun loadEmojisUsageStatistics() {
        val preferences =
            context.getSharedPreferences(EMOJIS_USAGE_STATISTICS_PREFERENCE_NAME, Context.MODE_PRIVATE)
        usageStatistics.clear()
        usageStatistics.putAll(preferences.all.mapValues { it.value as Int })
    }

    private fun saveEmojisUsageStatistics(commit: Boolean = false) {
        val preferences =
            context.getSharedPreferences(EMOJIS_USAGE_STATISTICS_PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit) {
            usageStatistics.entries.forEach { putInt(it.key, it.value) }
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun loadEmojisSkinColors() {
        val preferences = context.getSharedPreferences(EMOJIS_SKIN_COLORS_PREFERENCE_NAME, Context.MODE_PRIVATE)
        skinColors.clear()
        skinColors.putAll(preferences.all.mapValues { it.value as String })
        defaultSkinColor = preferences.getString("_default", "")
    }

    private fun saveEmojisSkinColors(commit: Boolean = false) {
        val preferences = context.getSharedPreferences(EMOJIS_SKIN_COLORS_PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit) {
            clear()
            if (defaultSkinColor.isNotEmpty())
                putString("_default", defaultSkinColor)
            skinColors.entries.forEach { putString(it.key, it.value) }
        }
    }
    //endregion

    private val cellSize by lazy { context.resources.getDimensionPixelOffset(R.dimen.emoji_cell_size) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        layoutManager.spanCount = MeasureSpec.getSize(widthMeasureSpec) / cellSize
    }

    override fun getIconResource(): Int = R.drawable.ic_round_smile_24dp

    override fun onBackspacePressed() {
        if (txtBoundWith?.text?.isEmpty() == true)
            return
        txtBoundWith?.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
    }

    override fun setTargetCoordinatorLayout(target: CoordinatorLayout) {
        containerView.findViewById<ScrollReplicatorCoordinatorLayout>(R.id.container).targetLayout = target
    }

}