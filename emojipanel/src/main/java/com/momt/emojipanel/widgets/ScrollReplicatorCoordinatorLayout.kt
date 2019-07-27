package com.momt.emojipanel.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * As same as [CoordinatorLayout] but sends nested scroll events to the [targetLayout]
 */
class ScrollReplicatorCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    var targetLayout: CoordinatorLayout? = null

    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        targetLayout?.onNestedFling(target, velocityX, velocityY, consumed)
        return super.onNestedFling(target, velocityX, velocityY, consumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        targetLayout?.onNestedPreScroll(target, dx, dy, consumed)
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        targetLayout?.onNestedPreScroll(target, dx, dy, consumed, type)
        super.onNestedPreScroll(target, dx, dy, consumed, type)
    }

    override fun onStopNestedScroll(target: View) {
        targetLayout?.onStopNestedScroll(target)
        super.onStopNestedScroll(target)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        targetLayout?.onStopNestedScroll(target, type)
        super.onStopNestedScroll(target, type)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        targetLayout?.onStartNestedScroll(child, target, nestedScrollAxes)
        return super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        targetLayout?.onStartNestedScroll(child, target, axes, type)
        return super.onStartNestedScroll(child, target, axes, type)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        targetLayout?.onNestedScrollAccepted(child, target, nestedScrollAxes)
        super.onNestedScrollAccepted(child, target, nestedScrollAxes)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int, type: Int) {
        targetLayout?.onNestedScrollAccepted(child, target, nestedScrollAxes, type)
        super.onNestedScrollAccepted(child, target, nestedScrollAxes, type)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        targetLayout?.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        targetLayout?.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        targetLayout?.onNestedPreFling(target, velocityX, velocityY)
        return super.onNestedPreFling(target, velocityX, velocityY)
    }
}