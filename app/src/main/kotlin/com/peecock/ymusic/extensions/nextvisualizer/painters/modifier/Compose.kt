package com.peecock.ymusic.extensions.nextvisualizer.painters.modifier

import android.graphics.Canvas
import android.graphics.Paint
import com.peecock.ymusic.extensions.nextvisualizer.painters.Painter
import com.peecock.ymusic.extensions.nextvisualizer.utils.VisualizerHelper

class Compose(vararg val painters: Painter) : Painter() {
    override var paint = Paint()

    override fun calc(helper: VisualizerHelper) {
        painters.forEach { painter ->
            painter.calc(helper)
        }
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        painters.forEach { painter ->
            painter.paint.apply { colorFilter = paint.colorFilter;xfermode = paint.xfermode }
            painter.draw(canvas, helper)
        }
    }
}