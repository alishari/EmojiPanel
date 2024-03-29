package com.momt.emojipanel.emoji

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class EmojiSpanNew(emojiCode: CharSequence, private val sizeRatio: Float = 1f) : ReplacementSpan() {

    private val drawable = EmojiDrawable.getEmojiDrawable(emojiCode)!!


    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val fontMetricsInt = paint.fontMetricsInt
        fm?.apply {
            ascent = fontMetricsInt.ascent
            descent = fontMetricsInt.descent
            top = fontMetricsInt.top
            bottom = fontMetricsInt.bottom
            leading = fontMetricsInt.leading
        }
        val size = ((fontMetricsInt.descent - fontMetricsInt.ascent) * sizeRatio).toInt()
        drawable.setBounds(0, 0, size, size)
        return size
    }

    override fun draw(
        canvas: Canvas, text: CharSequence?, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int,
        paint: Paint
    ) {
        canvas.save()
        val fontMetricsInt = paint.fontMetricsInt
        canvas.translate(
            x,
            (y + fontMetricsInt.ascent).toFloat() +
                    (fontMetricsInt.descent - fontMetricsInt.ascent - drawable.bounds.height()) / 2f
        )
        drawable.draw(canvas)
        canvas.restore()
    }

}