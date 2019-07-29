package com.momt.emojipanel.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.momt.emojipanel.R

/**
 * @property basedOnWidth Sets to make the view square based on either width or height
 */
open class SquareImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var basedOnWidth: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.obtainStyledAttributes(attrs, intArrayOf(R.attr.basedOnWidth))
            .apply { this@SquareImageView.basedOnWidth = getBoolean(0, true) }
            .also { it.recycle() }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (basedOnWidth)
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        else
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
    }

}