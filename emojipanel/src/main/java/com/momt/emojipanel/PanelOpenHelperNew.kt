package com.momt.emojipanel

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.edit
import androidx.core.view.updateLayoutParams
import com.momt.emojipanel.utils.EmptyActivityLifecycleCallbacks
import com.momt.emojipanel.utils.EventHandler

/**
 * @property isKeyboardOpen Indicates whether the helper is considering keyboard is open or not
 * @property isPanelShowing Indicates whether the user is seeing the panel or not
 */
class PanelOpenHelperNew(
    var context: Activity,
    val txt: EditText,
    val panel: View,
    val btn: ImageView,
    val nonEmojiContent: View,
    val parentFrame: FrameLayout
) {
    companion object {
        private const val PANEL_HEIGHT_PREFERENCE_NAME = "emojiPanelHeight"
        private const val PANEL_P_HEIGHT_PREFERENCE_KEY = "portraitHeight"
        private const val PANEL_L_HEIGHT_PREFERENCE_KEY = "landscapeHeight"
    }

    private var panelPortraitHeight: Int = 0
    private var panelLandscapeHeight: Int = 0

    var isKeyboardOpen = false
        private set

    val panelVisibilityChanged = EventHandler<PanelOpenHelperNew, Boolean>()
    var isPanelShowing = false
        private set(value) {
            if (field != value) {
                panelVisibilityChanged(this, value)
                field = value
            }
        }

    private val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastKeyboardHeight = 0
        private var lastOrientation = isActivityPortrait

        override fun onGlobalLayout() {
            val keyboardHeight = getKeyboardHeight()
            val isPortrait = isActivityPortrait

            try {
                if (lastKeyboardHeight == keyboardHeight && lastOrientation == isPortrait)
                    return

                if (keyboardHeight == 0) {
                    isKeyboardOpen = false
                    if (!isPanelShowing) {
                        nonEmojiContent.updateLayoutParams { height = ViewGroup.LayoutParams.MATCH_PARENT }
                        hidePanel()
                    }
                    return
                } else {
                    if (isPortrait) panelPortraitHeight = keyboardHeight
                    else panelLandscapeHeight = keyboardHeight
                    updatePanelHeight(isPortrait)

                    isKeyboardOpen = true
                    panel.visibility = View.VISIBLE
                    isPanelShowing = false
                    nonEmojiContent.updateLayoutParams { height = parentFrame.height }
                }
            } finally {
                lastKeyboardHeight = keyboardHeight
                lastOrientation = isPortrait
            }
        }
    }

    private val pauseResumeChecker = object : EmptyActivityLifecycleCallbacks() {
        private var lastIsKeyboardOpen = false
        private var lastIsPanelShowing = false

        override fun onActivityPaused(activity: Activity?) {
            if (activity != context) return
            lastIsKeyboardOpen = isKeyboardOpen
            lastIsPanelShowing = isPanelShowing
        }

        override fun onActivityResumed(activity: Activity?) {
            if (activity != context) return
            if (lastIsKeyboardOpen) {
                activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                openKeyboard()
            } else if (!lastIsKeyboardOpen && lastIsPanelShowing) {
                activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                openPanel()
            }
        }
    }

    init {
        loadSettings()
        parentFrame.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        updatePanelHeight(isActivityPortrait)

        context.application.registerActivityLifecycleCallbacks(pauseResumeChecker)
    }

    private fun updatePanelHeight(isPortrait: Boolean) {
        panel.updateLayoutParams {
            height = if (isPortrait) panelPortraitHeight else panelLandscapeHeight
            (this as ViewGroup.MarginLayoutParams).topMargin = parentFrame.height
        }
    }

    private val isActivityPortrait
        get() = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private val isPanelVisible: Boolean
        get() = panel.visibility == View.VISIBLE

    /**
     * Switches the panel to keyboard or panel. Default starts with showing the panel
     */
    fun switchPanel() {
        when {
            isKeyboardOpen -> showPanel()
            panel.visibility != View.GONE -> openKeyboard()
            else -> openPanel()
        }
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(txt.windowToken, 0)
    }

    private fun openKeyboard() {
        txt.requestFocus()
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(txt, InputMethodManager.SHOW_IMPLICIT)
        btn.setImageResource(R.drawable.ic_round_smile_24dp)
    }

    /**
     * Opens the panel when the keyboard isn't open. Do not use this method when keyboard is open.
     */
    fun openPanel() {
        panel.updateLayoutParams {
            height = if (isActivityPortrait) panelPortraitHeight else panelLandscapeHeight
            (this as ViewGroup.MarginLayoutParams).topMargin = parentFrame.height - height
        }
        nonEmojiContent.updateLayoutParams { height = (panel.layoutParams as ViewGroup.MarginLayoutParams).topMargin }
        showPanel()
    }

    /**
     * Closes the panel when the keyboard isn't open. Do not use this method when keyboard is open.
     */
    fun closePanel() {
        hidePanel()
        isPanelShowing = false
        (txt.parent as View).updateLayoutParams { height = ViewGroup.LayoutParams.MATCH_PARENT }
    }

    private fun showPanel() {
        txt.requestFocus()
        panel.visibility = View.VISIBLE
        isPanelShowing = true
        hideKeyboard()
        btn.setImageResource(R.drawable.ic_round_keyboard_24dp)
    }

    private fun hidePanel() {
        panel.visibility = View.GONE
        isPanelShowing = false
        btn.setImageResource(R.drawable.ic_round_smile_24dp)
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

    private fun getKeyboardHeight(): Int {
        val rect = Rect()
        parentFrame.getWindowVisibleDisplayFrame(rect)
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = context.resources.getDimensionPixelSize(resourceId)

        val usableViewHeight =
            parentFrame.rootView.height - (if (rect.top != 0) statusBarHeight else 0) - getViewInset(txt.rootView)
        return usableViewHeight - rect.height()
    }

    private fun getViewInset(view: View?): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        if (view == null || Build.VERSION.SDK_INT < 21 || view.height == context.resources.displayMetrics.heightPixels || view.height == context.resources.displayMetrics.heightPixels - statusBarHeight)
            return 0
        try {
            val mAttachInfoField = View::class.java.getDeclaredField("mAttachInfo")
            mAttachInfoField.isAccessible = true

            val mAttachInfo = mAttachInfoField.get(view)
            if (mAttachInfo != null) {
                val mStableInsetsField = mAttachInfo.javaClass.getDeclaredField("mStableInsets")
                mStableInsetsField.isAccessible = true
                val insets = mStableInsetsField.get(mAttachInfo) as Rect
                return insets.bottom
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }
}