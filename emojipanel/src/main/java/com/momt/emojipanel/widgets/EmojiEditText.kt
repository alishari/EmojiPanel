package com.momt.emojipanel.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText

/**
 * An [EditText] which responses to emoji inserts and replaces them with [com.momt.emojipanel.emoji.EmojiSpan]
 */
class EmojiEditText : AppCompatEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        addTextChangedListener(EmojiReplacerTextWatcher())
    }
}