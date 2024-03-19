package com.xbot.clockview

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable

internal fun Drawable.drawFitCenter(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
    val drawableRatio = intrinsicWidth.toFloat() / intrinsicHeight
    val viewRatio = viewWidth.toFloat() / viewHeight

    val rect = Rect()

    if (drawableRatio > viewRatio) {
        val scaledHeight = (viewWidth / drawableRatio).toInt()
        rect.left = 0
        rect.top = (viewHeight - scaledHeight) / 2
        rect.right = viewWidth
        rect.bottom = rect.top + scaledHeight
    } else {
        val scaledWidth = (viewHeight * drawableRatio).toInt()
        rect.left = (viewWidth - scaledWidth) / 2
        rect.top = 0
        rect.right = rect.left + scaledWidth
        rect.bottom = viewHeight
    }

    bounds = rect
    draw(canvas)
}
