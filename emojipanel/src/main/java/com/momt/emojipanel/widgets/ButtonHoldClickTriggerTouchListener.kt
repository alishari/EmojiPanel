package com.momt.emojipanel.widgets

import android.view.MotionEvent
import android.view.View
import androidx.core.view.postDelayed
import kotlin.math.max

class ButtonHoldClickTriggerTouchListener(
    private val startDelay: Long = STANDARD_DELAY,
    private val minDelay: Long = STANDARD_MIN_DELAY_BETWEEN_TRIGGERS,
    private val delayReduce: Long = STANDARD_DELAY_REDUCE
) : View.OnTouchListener {
    companion object {
        const val STANDARD_DELAY = 350L
        const val STANDARD_MIN_DELAY_BETWEEN_TRIGGERS = 50L
        const val STANDARD_DELAY_REDUCE = 100L
    }

    private var isActive = false
    private var performedOnce = false

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null || event == null) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isActive = true
                performedOnce = false
                performRepetitiveClick(v, startDelay)
                v.isPressed = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isActive = false
                if (!performedOnce) //Making sure at least one time click is called
                    v.performClick()
                v.isPressed = false
            }
            else -> return false
        }
        return true
    }

    private fun performRepetitiveClick(view: View, delay: Long) {
        view.postDelayed(delay) {
            if (!isActive)
                return@postDelayed
            performedOnce = true
            view.performClick()
            performRepetitiveClick(view, max(minDelay, delay - delayReduce))    //Preventing be faster than minDelay
        }
    }
}