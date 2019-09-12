package com.benoitthore.enamel.android.extract

import android.graphics.*
import android.view.View
import com.benoitthore.enamel.geometry.figures.ECircle
import com.benoitthore.enamel.geometry.figures.ELine
import com.benoitthore.enamel.geometry.figures.ERect
import com.benoitthore.enamel.geometry.figures.ERectMutable
import com.benoitthore.enamel.geometry.primitives.EPoint

fun ELine.toLinearGradient(
    colors: List<Int>,
    shaderMode: Shader.TileMode = Shader.TileMode.CLAMP
) =
    LinearGradient(start.x, start.y, end.x, end.y, colors.toIntArray(), null, shaderMode)


////
////
////

fun ERect.toAndroidRect(buffer: Rect) = buffer.apply {
    val rect = this@toAndroidRect
    left = rect.left.toInt()
    top = rect.top.toInt()
    right = rect.right.toInt()
    bottom = rect.bottom.toInt()
}

fun ERect.toAndroidRectF(buffer: RectF) = buffer.apply {
    val rect = this@toAndroidRectF
    left = rect.left
    top = rect.top
    right = rect.right
    bottom = rect.bottom
}

private val bufferRectF = RectF()

////
////
////
fun Canvas.drawCircles(circles: List<ECircle>, paint: Paint) =
    circles.forEach { drawCircle(it, paint) }

fun Canvas.drawCircle(circle: ECircle, paint: Paint) {
    drawCircle(circle.x, circle.y, circle.radius, paint)
}

fun Canvas.drawLine(line: ELine, paint: Paint) = drawLine(line.start, line.end, paint)

fun Canvas.drawLine(start: EPoint, end: EPoint, paint: Paint) {
    drawLine(start.x, start.y, end.x, end.y, paint)
}

fun Canvas.drawOvals(rects: List<ERect>, paint: Paint) = rects.forEach { drawOval(it, paint) }
fun Canvas.drawOval(rect: ERect, paint: Paint) {
    synchronized(bufferRectF) {
        drawOval(rect.toAndroidRectF(bufferRectF), paint)
    }
}

fun Canvas.drawRects(rects: List<ERect>, paint: Paint) = rects.forEach { drawRect(it, paint) }
fun Canvas.drawRect(rect: ERect, paint: Paint) {
    synchronized(bufferRectF) {
        drawRect(rect.toAndroidRectF(bufferRectF), paint)
    }
}

fun Canvas.drawRoundRect(rects: List<ERect>, rx: Number, ry: Number, paint: Paint) =
    rects.forEach { drawRoundRect(it, rx, ry, paint) }

fun Canvas.drawRoundRect(rect: ERect, rx: Number, ry: Number, paint: Paint) {
    synchronized(bufferRectF) {
        drawRoundRect(
            rect.toAndroidRectF(bufferRectF),
            rx.toFloat(),
            ry.toFloat(),
            paint
        )
    }
}