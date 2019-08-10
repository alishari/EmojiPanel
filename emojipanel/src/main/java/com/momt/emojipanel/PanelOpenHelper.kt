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
 * The helper class to open and close the panel at bottom of a edittext. It handles keyboard size, changes panel size,
 * switches between panel and keyboard, ...
 * @param activity The activity to run in
 * @param txt The [EditText] which gets focus
 * @param panel The panel to be opened and closed
 * @param switchButton The [ImageView] which the image of will change on panel switches
 * @param nonEmojiContent The rest of the layout to be resized when the [panel] or keyboard is open
 * @param parentFrame The [FrameLayout] container of the [nonEmojiContent] and the [panel]
 * @property isKeyboardOpen Indicates whether the helper is considering keyboard is open or not
 * @property isPanelShowing Indicates whether the user is seeing the panel or not
 * @property panelVisibilityChanged EventHandler for panel visibility change. The event arg indicates that panel is visible (true) or not
 */
class PanelOpenHelper(
    var activity: Activity,
    val txt: EditText,
    val panel: View,
    val switchButton: ImageView,
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

    data class PanelVisibilityChangeEventArgs(val isVisible: Boolean)

    val panelVisibilityChanged = EventHandler<PanelOpenHelper, PanelVisibilityChangeEventArgs>()
    var isPanelShowing = false
        private set(value) {
            if (field != value) {
                panelVisibilityChanged(this, PanelVisibilityChangeEventArgs(value))
                field = value
            }
        }

    var panelIconProvider: DrawableProvider = DrawableProvider.of(R.drawable.ic_round_smile_24dp)
    var keyboardIconProvider: DrawableProvider = DrawableProvider.of(R.drawable.ic_round_keyboard_24dp)

    private val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastKeyboardHeight = 0
        private var lastOrientation = isActivityPortrait
        private var lastParentFrameHeight = parentFrame.height

        override fun onGlobalLayout() {
            val keyboardHeight = getKeyboardHeight()
            val isPortrait = isActivityPortrait

            try {
                if (lastKeyboardHeight == keyboardHeight && lastOrientation == isPortrait)
                    return

                if (keyboardHeight == 0) {
                    isKeyboardOpen = false
                    if (!isPanelShowing)   //Keyboard is closed using back button
                        closePanel()
                    else if (lastParentFrameHeight != parentFrame.height) //and panel is showing
                    //(happens at orientation change for example)
                        openPanel()

                    return
                } else {
                    if (isPortrait) panelPortraitHeight = keyboardHeight
                    else panelLandscapeHeight = keyboardHeight
                    updatePanelHeight(isPortrait)

                    isKeyboardOpen = true
                    isPanelShowing = false
                    nonEmojiContent.updateLayoutParams { height = parentFrame.height }
                }
            } finally {
                lastKeyboardHeight = keyboardHeight
                lastOrientation = isPortrait
                lastParentFrameHeight = parentFrame.height
            }
        }
    }

    private val pauseResumeChecker = object : EmptyActivityLifecycleCallbacks() {
        private var lastIsKeyboardOpen = false
        private var lastIsPanelShowing = false

        override fun onActivityPaused(activity: Activity?) {
            if (activity != this@PanelOpenHelper.activity) return
            lastIsKeyboardOpen = isKeyboardOpen
            lastIsPanelShowing = isPanelShowing
        }

        override fun onActivityResumed(activity: Activity?) {
            if (activity != this@PanelOpenHelper.activity) return
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

        activity.application.registerActivityLifecycleCallbacks(pauseResumeChecker)

        panelVisibilityChanged += { _, args ->
            switchButton.setImageDrawable(
                (if (args.isVisible) keyboardIconProvider else panelIconProvider)
                    .getDrawable(activity)
            )
        }
    }

    private fun updatePanelHeight(isPortrait: Boolean) {
        panel.updateLayoutParams {
            height = if (isPortrait) panelPortraitHeight else panelLandscapeHeight
            (this as ViewGroup.MarginLayoutParams).topMargin = parentFrame.height
        }
    }

    private val isActivityPortrait
        get() = activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

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
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(txt.windowToken, 0)
    }

    private fun openKeyboard() {
        txt.requestFocus()
        val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(txt, InputMethodManager.SHOW_IMPLICIT)
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
        nonEmojiContent.updateLayoutParams { height = ViewGroup.LayoutParams.MATCH_PARENT }
    }

    private fun showPanel() {
        txt.requestFocus()
        panel.visibility = View.VISIBLE
        isPanelShowing = true
        hideKeyboard()
    }

    private fun hidePanel() {
        panel.visibility = View.GONE
        isPanelShowing = false
    }

    fun loadSettings() {
        val preferences = activity.getSharedPreferences(PANEL_HEIGHT_PREFERENCE_NAME, Context.MODE_PRIVATE)
        panelPortraitHeight = preferences.getInt(
            PANEL_P_HEIGHT_PREFERENCE_KEY,
            activity.resources.getDimensionPixelOffset(R.dimen.panel_default_height_portrait)
        )
        panelLandscapeHeight = preferences.getInt(
            PANEL_L_HEIGHT_PREFERENCE_KEY,
            activity.resources.getDimensionPixelOffset(R.dimen.panel_default_height_landscape)
        )
    }

    fun saveSettings(commit: Boolean = false) {
        val preferences = activity.getSharedPreferences(PANEL_HEIGHT_PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit(commit) {
            putInt(
                PANEL_P_HEIGHT_PREFERENCE_KEY,
                if (panelPortraitHeight > 0) panelPortraitHeight
                else activity.resources.getDimensionPixelOffset(R.dimen.panel_default_height_portrait)
            )
            putInt(
                PANEL_L_HEIGHT_PREFERENCE_KEY,
                if (panelLandscapeHeight > 0) panelLandscapeHeight
                else activity.resources.getDimensionPixelOffset(R.dimen.panel_default_height_landscape)
            )
        }
    }

    private fun getKeyboardHeight(): Int {
        val rect = Rect()
        parentFrame.getWindowVisibleDisplayFrame(rect)
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = activity.resources.getDimensionPixelSize(resourceId)

        val usableViewHeight =
            parentFrame.rootView.height - (if (rect.top != 0) statusBarHeight else 0) - getViewInset(txt.rootView)
        return usableViewHeight - rect.height()
    }

    private fun getViewInset(view: View?): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = activity.resources.getDimensionPixelSize(resourceId)
        if (view == null || Build.VERSION.SDK_INT < 21 || view.height == activity.resources.displayMetrics.heightPixels || view.height == activity.resources.displayMetrics.heightPixels - statusBarHeight)
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