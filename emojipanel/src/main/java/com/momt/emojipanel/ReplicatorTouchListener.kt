package com.momt.emojipanel

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

/**
 * A [View.OnTouchListener] that just replicates the events to the [target]
 * @param target The target view which events will be reflected to
 */
class ReplicatorTouchListener(var target: View? = null) : View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        target?.dispatchTouchEvent(
            MotionEvent.obtain(
                event!!.downTime,
                event.eventTime,
                event.action,
                event.rawX,
                event.rawY,
                event.metaState
            )
        )
        return false
    }

}