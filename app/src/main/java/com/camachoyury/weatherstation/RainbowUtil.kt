package com.camachoyury.weatherstation

import android.graphics.Color

/**
 * Created by yury on 7/11/17.
 */
public class RainbowUtil {


    /* LED Strip Color Constants*/

companion object {

    /* Barometer Range Constants */
    val BAROMETER_RANGE_LOW = 965f
    val BAROMETER_RANGE_HIGH = 1035f
    fun stripRainbowColor():IntArray{

       val  sRainbowColors = IntArray(7)
        for (i in sRainbowColors.indices) {
            val hsv = floatArrayOf(i * 360f / sRainbowColors.size, 1.0f, 1.0f)
            sRainbowColors[i] = Color.HSVToColor(255, hsv)
        }
        return sRainbowColors
    }

    fun getWeatherStripColors(pressure: Float): IntArray {
        val t = (pressure - BAROMETER_RANGE_LOW) / (BAROMETER_RANGE_HIGH - BAROMETER_RANGE_LOW)
        var n = Math.ceil((stripRainbowColor().size * t).toDouble()).toInt()
        n = Math.max(0, Math.min(n, stripRainbowColor().size))

        val colors = IntArray(stripRainbowColor().size)
        for (i in 0..n - 1) {
            val ri = stripRainbowColor().size - 1 - i
            colors[ri] = stripRainbowColor()[ri]
        }

        return colors
    }
}
}
