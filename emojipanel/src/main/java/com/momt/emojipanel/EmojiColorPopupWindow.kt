package com.momt.emojipanel

import android.content.Context
import android.os.Build
import android.widget.PopupWindow
import com.momt.emojipanel.utils.EventHandler
import com.momt.emojipanel.widgets.EmojiSkinColorLayout

internal class EmojiColorPopupWindow(context: Context, baseCode: String) :
    PopupWindow(context) {

    companion object {
        private var popupHeight = -1
        fun getPopupHeight(context: Context): Int {
            if (popupHeight < 0)
                popupHeight = (context.resources.getDimension(R.dimen.emoji_cell_size) +
                        2 * context.resources.getDimension(R.dimen.skin_color_select_popup_padding)).toInt()
            return popupHeight
        }
    }

    val itemClicked = EventHandler<EmojiColorPopupWindow, EmojiSkinColorLayout.ItemClickEventArgs>()
    val dismissed = EventHandler<EmojiColorPopupWindow, Any?>()

    init {
        contentView = EmojiSkinColorLayout(context).apply {
            baseEmojiCode = baseCode
            itemClicked += { _, args ->
                this@EmojiColorPopupWindow.itemClicked(this@EmojiColorPopupWindow, args)
                dismiss()
            }
            selectionCanceled += { _, _ -> dismiss() }
        }
        setBackgroundDrawable(
            if (Build.VERSION.SDK_INT >= 21)
                context.resources.getDrawable(
                    R.drawable.emoji_skin_color_select_popup_background,
                    context.theme
                )
            else
                context.resources.getDrawable(R.drawable.emoji_skin_color_select_popup_background)
        )
        if (Build.VERSION.SDK_INT >= 21)
            elevation = context.resources.getDimension(R.dimen.skin_color_select_popup_elevation)
    }

    override fun dismiss() {
        super.dismiss()
        dismissed(this, null)
    }
}