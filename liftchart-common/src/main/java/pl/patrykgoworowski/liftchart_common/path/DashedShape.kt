package pl.patrykgoworowski.liftchart_common.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

class DashedShape(
    val shape: Shape,
    val dashLength: Float,
    val gapLength: Float,
    val fitStrategy: FitStrategy = FitStrategy.Resize
) : Shape {

    private var drawDashLength = dashLength
    private var drawGapLength = gapLength
    private val tempBounds = RectF()

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF,
    ) {
        if (bounds.width() > bounds.height()) {
            drawHorizontalDashes(canvas, paint, path, bounds)
        } else {
            drawVerticalDashes(canvas, paint, path, bounds)
        }
    }

    private fun drawHorizontalDashes(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF,
    ) {
        drawDashes(
            canvas = canvas,
            paint = paint,
            path = path,
            length = bounds.width(),
            drawStart = bounds.left,
        ) { shapeBounds, currentDrawStart ->
            shapeBounds.set(
                currentDrawStart,
                bounds.top,
                currentDrawStart + drawDashLength,
                bounds.bottom
            )
        }
    }

    private fun drawVerticalDashes(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF,
    ) {
        drawDashes(
            canvas = canvas,
            paint = paint,
            path = path,
            length = bounds.height(),
            drawStart = bounds.top,
        ) { shapeBounds, currentDrawStart ->
            shapeBounds.set(
                bounds.left,
                currentDrawStart,
                bounds.right,
                currentDrawStart + drawDashLength
            )
        }
    }

    private fun calculateDrawLengths(length: Float) = when (fitStrategy) {
        FitStrategy.Resize -> {
            when {
                length < dashLength + gapLength -> {
                    drawDashLength = length
                    drawGapLength = 0f
                }
                else -> {
                    var fitWidth = dashLength
                    while (length > fitWidth) {
                        fitWidth += gapLength + dashLength
                    }
                    val ratio = length / fitWidth
                    drawDashLength = dashLength * ratio
                    drawGapLength = gapLength * ratio
                }
            }
        }
        FitStrategy.Fixed -> {
            drawDashLength = dashLength
            drawGapLength = gapLength
        }
    }

    private inline fun drawDashes(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        length: Float,
        drawStart: Float,
        setBounds: (shapeBounds: RectF, currentDrawStart: Float) -> Unit,
    ) {
        calculateDrawLengths(length)

        var index = 0
        var drawnLength = 0f
        while (length - drawnLength > 0) {
            if (index % 2 == 0) {
                setBounds(tempBounds, drawStart + drawnLength)
                path.reset()
                shape.drawShape(canvas, paint, path, tempBounds)
                drawnLength += drawDashLength
            } else {
                drawnLength += drawGapLength
            }
            index++
        }
    }

    enum class FitStrategy {
        Resize,
        Fixed
    }

}