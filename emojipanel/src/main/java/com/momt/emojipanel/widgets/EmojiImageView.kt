package com.momt.emojipanel.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.util.component1
import androidx.core.util.component2
import com.momt.emojipanel.R
import com.momt.emojipanel.emoji.EmojiDrawable
import com.momt.emojipanel.emoji.EmojiUtils

/**
 * @property emojiCode Code of the emoji to be shown
 * @property showSkinColor Indicates whether or not to show the colored circle as skin color
 */
class EmojiImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SquareImageView(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private lateinit var skinColors: IntArray
        private fun loadSkinColors(context: Context) {
            if (this::skinColors.isInitialized) return
            skinColors = intArrayOf(
                ContextCompat.getColor(context, R.color.skin_color_none),
                ContextCompat.getColor(context, R.color.skin_color_light),
                ContextCompat.getColor(context, R.color.skin_color_medium_light),
                ContextCompat.getColor(context, R.color.skin_color_medium),
                ContextCompat.getColor(context, R.color.skin_color_medium_dark),
                ContextCompat.getColor(context, R.color.skin_color_dark)
            )
        }
    }

    init {
        loadSkinColors(context)
    }

    init {
        val ta = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        background = ta.getDrawable(0)
        ta.recycle()
        scaleType = ScaleType.CENTER
    }

    var emojiCode: String = ""
        set(value) {
            field = value
            setImageDrawable(EmojiDrawable.getEmojiBigDrawable(value))
            setSkinColor()
            invalidate()
        }


    var showSkinColor = false
        set(value) {
            field = value
            setSkinColor()
            invalidate()
        }

    private val skinColorOvalPaint = Paint()

    private fun setSkinColor() {
        if (!showSkinColor) return
        val (_, color) = EmojiUtils.extractRawAndColorFromCode(emojiCode)
        skinColorOvalPaint.color = skinColors[EmojiUtils.skinColors.indexOf(color)]
    }


    private var skinColorOvalRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        skinColorOvalRect = RectF(
            w * 8f / 10f,
            h * 8f / 10f,
            w * 9f / 10f,
            h * 9f / 10f
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (showSkinColor)
            canvas?.drawOval(skinColorOvalRect, skinColorOvalPaint)
    }
}