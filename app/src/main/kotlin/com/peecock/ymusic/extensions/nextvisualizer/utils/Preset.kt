package com.peecock.ymusic.extensions.nextvisualizer.utils

import android.graphics.*
import com.peecock.ymusic.extensions.nextvisualizer.painters.Painter
import com.peecock.ymusic.extensions.nextvisualizer.painters.fft.FftBar
import com.peecock.ymusic.extensions.nextvisualizer.painters.fft.FftCLine
import com.peecock.ymusic.extensions.nextvisualizer.painters.fft.FftCWaveRgb
import com.peecock.ymusic.extensions.nextvisualizer.painters.misc.Background
import com.peecock.ymusic.extensions.nextvisualizer.painters.misc.Icon
import com.peecock.ymusic.extensions.nextvisualizer.painters.modifier.Compose
import com.peecock.ymusic.extensions.nextvisualizer.painters.modifier.Rotate
import com.peecock.ymusic.extensions.nextvisualizer.painters.modifier.Scale
import com.peecock.ymusic.extensions.nextvisualizer.painters.modifier.Shake

class Preset {
    companion object {

        /**
         * Feel free to add your awesome preset here ;)
         * Hint: You can use `Compose` painter to group multiple painters together as a single painter
         */
        fun getPreset(name: String): Painter {
            return when (name) {
                "debug" -> FftBar()
                else -> FftBar()
            }
        }

        fun getPresetWithBitmap(name: String, bitmap: Bitmap): Painter {
            return when (name) {
                "cIcon" -> Compose(Rotate(FftCLine()), Icon(Icon.getCircledBitmap(bitmap)))
                "cWaveRgbIcon" -> Compose(
                    Rotate(FftCWaveRgb()),
                    Icon(Icon.getCircledBitmap(bitmap))
                )
                "liveBg" -> Scale(Shake(Background(bitmap)), scaleX = 1.02f, scaleY = 1.02f)
                "debug" -> Icon(bitmap)
                else -> Icon(bitmap)
            }
        }
    }
}