package com.momt.emojipanel.widgets

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import com.momt.emojipanel.AndroidUtilities
import com.momt.emojipanel.emoji.EmojiUtils

/**
 * An [EditText] which responses to emoji inserts and replaces them with [com.momt.emojipanel.emoji.EmojiSpan]
 */
class EmojiEditText : AppCompatEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null)
                    return

                removeTextChangedListener(this)

                s.replace(
                    changeStart,
                    changeEnd,
                    EmojiUtils.replaceEmoji(
                        s.subSequence(changeStart, changeEnd),
                        paint.fontMetricsInt,
                        AndroidUtilities.dp(20f),
                        false,
                        intArrayOf(1)
                    )
                )

                addTextChangedListener(this)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing to do
            }

            private var changeStart = 0
            private var changeEnd = 0
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeStart = start
                changeEnd = start + count
            }
        })
    }
}