package com.momt.emojipanel.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.momt.emojipanel.R
import com.momt.emojipanel.emoji.EmojiUtils
import com.momt.emojipanel.utils.EventHandler
import com.momt.emojipanel.utils.getRectOnScreen

/**
 * A horizontal [LinearLayout] which shows emojis with different skin colors in a row
 * @property baseEmojiCode The base emoji with no skin color (yellow) which skin colors will be applied to
 */
internal class EmojiSkinColorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        const val SKIN_COLOR_COUNT = 6
    }

    private val imgSize by lazy { context.resources.getDimensionPixelOffset(R.dimen.emoji_cell_size) }

    private val imgs: ArrayList<EmojiImageView>

    init {
        orientation = HORIZONTAL
        setPadding(context.resources.getDimensionPixelOffset(R.dimen.skin_color_select_popup_padding))
        imgs = ArrayList(SKIN_COLOR_COUNT)
        for (i in 0 until SKIN_COLOR_COUNT)
            addView(EmojiImageView(context).also { imgs.add(it); it.basedOnWidth = true }, imgSize, imgSize)
        imgs.forEach { img ->
            img.setOnClickListener { itemClicked(this, ItemClickEventArgs(img.emojiCode)) }
        }
    }

    var baseEmojiCode: String = ""
        set(value) =
            imgs.forEachIndexed { index, img -> img.emojiCode = EmojiUtils.setColorToCode(value, index) }

    data class ItemClickEventArgs(val selectedCode: String)

    val itemClicked = EventHandler<EmojiSkinColorLayout, ItemClickEventArgs>()
    val selectionCanceled = EventHandler<EmojiSkinColorLayout, Any?>()

    private var lastTouchedViewIndex = -1
    private var cachedRects: ArrayList<Rect> = arrayListOf()
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null)
            return false

        if (cachedRects.isEmpty() || cachedRects[0].isEmpty) {
            cachedRects = ArrayList(imgs.size)
            imgs.forEach { cachedRects.add(it.getRectOnScreen()) }
        }

        if (lastTouchedViewIndex != -1 && cachedRects[lastTouchedViewIndex].contains(ev.rawX.toInt(), ev.rawY.toInt()))
            return imgs[lastTouchedViewIndex].dispatchTouchEvent(copyMotionEventByView(ev, lastTouchedViewIndex))
        else {
            //Cancelling the previous item
            if (lastTouchedViewIndex != -1)
                copyMotionEventByView(ev, lastTouchedViewIndex, MotionEvent.ACTION_CANCEL)
                    .also { imgs[lastTouchedViewIndex].dispatchTouchEvent(it) }
                    .recycle()

            //Searching for new item
            var selectedViewIndex: Int = -1
            for (i in 0 until imgs.size)
                if (cachedRects[i].contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    selectedViewIndex = i
                    break
                }
            lastTouchedViewIndex = selectedViewIndex

            return if (selectedViewIndex != -1) {
                //Telling new txt a virtual down
                val newEvent = copyMotionEventByView(ev, selectedViewIndex, MotionEvent.ACTION_DOWN)
                    .also { println(it) }
                val result = imgs[selectedViewIndex].dispatchTouchEvent(newEvent)
                newEvent.recycle()
                result
            } else {
                //Cancel selection if up or cancel outside
                if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL)
                    selectionCanceled(this, null)
                true
            }
        }
    }

    private fun copyMotionEventByView(src: MotionEvent, index: Int, newAction: Int = src.action): MotionEvent {
        return MotionEvent.obtain(
            src.downTime,
            src.eventTime,
            newAction,
            src.rawX - cachedRects[index].left,
            src.rawY - cachedRects[index].top,
            src.metaState
        )
    }
}