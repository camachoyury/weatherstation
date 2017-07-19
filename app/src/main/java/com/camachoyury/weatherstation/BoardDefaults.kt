package com.camachoyury.weatherstation

import android.os.Build

/**
 * Created by yury on 7/11/17.
 */

class BoardDefaults {

    companion object {

        private val DEVICE_RPI3 = "rpi3"
        private val DEVICE_IMX7 = "imx7d_pico"

        fun getI2cBus(): String {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "I2C1"
                DEVICE_IMX7 -> return "I2C1"
                else -> throw IllegalArgumentException("Unsupported device: " + Build.DEVICE)
            }
        }

        fun getSpiBus(): String {
            when (Build.DEVICE) {
                DEVICE_RPI3 -> return "SPI0.0"
                DEVICE_IMX7 -> return "SPI3.1"
                else -> throw IllegalArgumentException("Unsupported device: " + Build.DEVICE)
            }
        }
    }


}
