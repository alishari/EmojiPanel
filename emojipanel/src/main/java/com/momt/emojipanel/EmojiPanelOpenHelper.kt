package com.momt.emojipanel

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.edit
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.momt.emojipanel.widgets.EmojiPanel

@Deprecated("This class doesn't work properly.")
class EmojiPanelOpenHelper(
    var context: Activity,
    val txt: EditText,
    val panel: EmojiPanel,
    val targetFullSizeView: View
) {

    companion object {
        private const val PANEL_HEIGHT_PREFERENCE_NAME = "emojiPanelHeight"
        private const val PANEL_P_HEIGHT_PREFERENCE_KEY = "portraitHeight"
        private const val PANEL_L_HEIGHT_PREFERENCE_KEY = "landscapeHeight"
    }

    private var panelPortraitHeight: Int = 0
    private var panelLandscapeHeight: Int = 0

    var isKeyboardOpen = false

    private val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastPortraitRect: Rect? = null
        private var lastLandscapeRect: Rect? = null

        override fun onGlobalLayout() {
            val newRect = Rect()
            txt.getWindowVisibleDisplayFrame(newRect)

            val isPortrait = isActivityPortrait
            val lastRect = if (isPortrait) lastPortraitRect else lastLandscapeRect

            if (lastRect == newRect)
                return

            if (isPortrait) lastPortraitRect = newRect
            else lastLandscapeRect = newRect

            if (lastRect == null)
                return

            if (newRect.width() != lastRect.width())     //is not a keyboard open/close
                return

//            val fullRect = Rect()
//            targetFullSizeView.getGlobalVisibleRect(fullRect)
//            val newHeight = fullRect.bottom - newRect.bottom

//            if (fullRect.bottom == newRect.bottom) {    //keyboard is closed
//                isKeyboardOpen = false
//                return
//            } else {
//                isKeyboardOpen = true
//                panel.visibility = View.GONE
//            }

            if (lastRect.height() < newRect.height()) {
                isKeyboardOpen = false
                return
            } else {
                isKeyboardOpen = true
//                panel.visibility = View.GONE
                (txt.parent as View).updateLayoutParams { height = (txt.parent as View).height }
            }

            val newHeight = lastRect.height() - newRect.height()

            val changed = panel.height != newHeight
            if (changed) {
                if (isPortrait) panelPortraitHeight = newHeight
                else panelLandscapeHeight = newHeight
                updatePanelHeight(isPortrait)
            }
        }
    }

    init {
        loadSettings()
        txt.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        updatePanelHeight(isActivityPortrait)
    }

    private fun updatePanelHeight(isPortrait: Boolean) {
        panel.getChildAt(0)
            .updateLayoutParams { height = if (isPortrait) panelPortraitHeight else panelLandscapeHeight }
        panel.updatePadding(top = panel.height)
    }

    private val isActivityPortrait
        get() = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val isPanelVisible: Boolean
        get() = panel.visibility == View.VISIBLE

    fun switchPanel() {
        when {
            isKeyboardOpen -> {
                openPanel()
            }
            panel.visibility != View.GONE -> openKeyboard()
            else -> openPanel()
        }
    }

    fun openPanel() {
        txt.requestFocus()
        panel.visibility = View.VISIBLE
        hideKeyboard()
    }

    fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(txt.windowToken, 0)
    }

    fun openKeyboard() {
        txt.requestFocus()
        closePanel()
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(txt, InputMethodManager.SHOW_IMPLICIT)
    }

    fun closePanel() {
        panel.visibility = View.GONE
    }

    fun loadSettings() {
        val preferences = context.getSharedPreferences(PANEL_HEIGHT_PREFERENCE_NAME, Context.MODE_PRIVATE)
        panelPortraitHeight = preferences.getInt(
            PANEL_P_HEIGHT_PREFERENCE_KEY,
            context.resources.getDimensionPixelOffset(R.dimen.panel_default_height_portrait)
        )
        panelLandscapeHeight = preferences.getInt(
            PANEL_L_HEIGHT_PREFERENCE_KEY,
            context.resources.getDimensionPixelOffset(R.dimen.panel_default_height_landscape)
        )
    }

    fun saveSettings(commit: Boolean = false) {
        val preferences = context.getSharedPreferences(PANEL_HEIGHT_PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit) {
            putInt(
                PANEL_P_HEIGHT_PREFERENCE_KEY,
                if (panelPortraitHeight > 0) panelPortraitHeight
                else context.resources.getDimensionPixelOffset(R.dimen.panel_default_height_portrait)
            )
            putInt(
                PANEL_L_HEIGHT_PREFERENCE_KEY,
                if (panelLandscapeHeight > 0) panelLandscapeHeight
                else context.resources.getDimensionPixelOffset(R.dimen.panel_default_height_landscape)
            )
        }
    }

    /*private fun getKeyboardHeight(): Int {
        val rect = Rect()
        txt.getWindowVisibleDisplayFrame(rect)
        val usableViewHeight =
            txt.rootView.height - (if (rect.top != 0) AndroidUtilities.statusBarHeight else 0) - AndroidUtilities.getViewInset(
                txt.rootView
            )
        return usableViewHeight - (rect.bottom - rect.top)
    }*/
}