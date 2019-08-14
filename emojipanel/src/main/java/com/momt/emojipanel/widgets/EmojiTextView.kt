package com.momt.emojipanel.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.momt.emojipanel.emoji.EmojiUtils

class EmojiTextView : AppCompatTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        text = EmojiUtils.replaceEmoji(
            text,
            false,
            intArrayOf(1)
        )
        addTextChangedListener(EmojiReplacerTextWatcher())
    }
}