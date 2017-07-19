package com.camachoyury.weatherstation

import android.app.Activity
import android.os.Bundle

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

import android.util.Log
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay


import java.io.IOException
import java.util.Arrays

class WeatherStationActivity : Activity() {

    // Peripheral drivers
    var mEnvironmentalSensorDriver: Bmx280SensorDriver? = null
    var mDisplay: AlphanumericDisplay? = null
    var mLedstrip: Apa102? = null

    private var mSensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Weather Station Started")

        mSensorManager = getSystemService(SensorManager::class.java)

        // Initialize temperature/pressure sensors
        try {
            mEnvironmentalSensorDriver = Bmx280SensorDriver(BoardDefaults.getI2cBus())
            // Register the drivers with the framework
            mEnvironmentalSensorDriver!!.registerTemperatureSensor()
            mEnvironmentalSensorDriver!!.registerPressureSensor()
            Log.d(TAG, "Initialized I2C BMP280")
        } catch (e: IOException) {
            throw RuntimeException("Error initializing BMP280", e)
        }

        // Initialize 7-segment display
        try {
            mDisplay = AlphanumericDisplay(BoardDefaults.getI2cBus())
            mDisplay!!.setEnabled(true)
            Log.d(TAG, "Initialized I2C Display")
        } catch (e: IOException) {
            throw RuntimeException("Error initializing display", e)
        }

        // Initialize LED strip
        try {
            mLedstrip = Apa102(BoardDefaults.getSpiBus(), Apa102.Mode.BGR)
            mLedstrip!!.setBrightness(LEDSTRIP_BRIGHTNESS)
            val colors = IntArray(7)
            Arrays.fill(colors, Color.RED)
            mLedstrip!!.write(colors)
            // Because of a known APA102 issue, write the initial value twice.
            mLedstrip!!.write(colors)
            Log.d(TAG, "Initialized SPI LED strip")
        } catch (e: IOException) {
            throw RuntimeException("Error initializing LED strip", e)
        }

    }

    override fun onStart() {
        super.onStart()

        // Register the BMP280 temperature sensor
        val temperature = mSensorManager!!
                .getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE)[0]

        mSensorManager!!.registerListener(mSensorEventListener, temperature,
                SensorManager.SENSOR_DELAY_NORMAL)

        // Register the BMP280 pressure sensor
        val pressure = mSensorManager!!
                .getDynamicSensorList(Sensor.TYPE_PRESSURE)[0]
        mSensorManager!!.registerListener(mSensorEventListener, pressure,
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onStop() {
        super.onStop()

        mSensorManager!!.unregisterListener(mSensorEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Close peripheral connections
        if (mEnvironmentalSensorDriver != null) {
            try {
                mEnvironmentalSensorDriver!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing sensors", e)
            } finally {
                mEnvironmentalSensorDriver = null
            }
        }

        if (mDisplay != null) {
            try {
                mDisplay!!.clear()
                mDisplay!!.setEnabled(false)
                mDisplay!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing display", e)
            } finally {
                mDisplay = null
            }
        }

        if (mLedstrip != null) {
            try {
                mLedstrip!!.write(IntArray(7))
                mLedstrip!!.setBrightness(0)
                mLedstrip!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing LED strip", e)
            } finally {
                mLedstrip = null
            }
        }
    }

    /**
     * Update the 7-segment display with the latest temperature value.

     * @param temperature Latest temperature value.
     */
    private fun updateTemperatureDisplay(temperature: Float) {

        if (mDisplay != null) {
            try {
                Log.e(TAG, " updating display ${ temperature.toString()}" )
                mDisplay!!.display(temperature.toDouble())
            } catch (e: IOException) {
                Log.e(TAG, "Error updating display", e)
            }

        }
    }

    /**
     * Update LED strip based on the latest pressure value.

     * @param pressure Latest pressure value.
     */
    private fun updateBarometerDisplay(pressure: Float) {

        if (mLedstrip != null) {
            try {
                val colors = RainbowUtil.getWeatherStripColors(pressure)
                mLedstrip!!.write(colors)
            } catch (e: IOException) {
                Log.e(TAG, "Error updating ledstrip", e)
            }

        }
    }

    // Callback when SensorManager delivers new data.
    private val mSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val value = event.values[0]


            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                updateTemperatureDisplay(value)
            } else if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                updateBarometerDisplay(value)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.d(TAG, "accuracy changed: " + accuracy)
        }
    }

    companion object {
        private val TAG = WeatherStationActivity::class.java.simpleName

        // Default LED brightness
        private val LEDSTRIP_BRIGHTNESS = 1
    }
}
