package com.momt.emojipanel.widgets

import android.text.Editable
import android.text.TextWatcher
import com.momt.emojipanel.AndroidUtilities
import com.momt.emojipanel.emoji.EmojiUtils

class EmojiReplacerTextWatcher : TextWatcher {

    private var enabled = true

    override fun afterTextChanged(s: Editable?) {
        if (!enabled || s == null)
            return

        enabled = false

        s.replace(
            changeStart,
            changeEnd,
            EmojiUtils.replaceEmoji(
                s.subSequence(changeStart, changeEnd),
                false,
                intArrayOf(1)
            )
        )

        enabled = true
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

}