package com.foursum.helper.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.foursum.helper.models.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SensorManagerUtil(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var onSensorDataChanged: ((SensorData) -> Unit)? = null
    private var sensorData = SensorData()

    private val sensorChannel = Channel<SensorEvent>(Channel.UNLIMITED)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())

    init {
//        val sampleRateMicros = 10_000 // 10ms = 100Hz
        // make it 5 ms
        val sampleRateMicros = 5_000 // 5ms = 200Hz
        val sensorTypes = listOf(
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD
        )

        sensorTypes.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let { sensor ->
                sensorManager.registerListener(this, sensor, sampleRateMicros, sampleRateMicros)
            }
        }

        // Launch a coroutine to process sensor events
        coroutineScope.launch {
            sensorChannel.receiveAsFlow().collect { event ->
                processSensorEvent(event)
            }
        }
    }

    fun setOnSensorDataChangedListener(listener: (SensorData) -> Unit) {
        onSensorDataChanged = listener
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Send sensor events to the channel
        coroutineScope.launch {
            sensorChannel.send(event)
        }
    }

    private fun processSensorEvent(event: SensorEvent) {
        val updatedValues = event.values.clone()
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> sensorData =
                sensorData.copy(linearAcceleration = updatedValues)

            Sensor.TYPE_GYROSCOPE -> sensorData = sensorData.copy(gyroscope = updatedValues)
            Sensor.TYPE_MAGNETIC_FIELD -> sensorData = sensorData.copy(magnetometer = updatedValues)
        }
        onSensorDataChanged?.invoke(sensorData)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun unregisterListeners() {
        sensorManager.unregisterListener(this)
        sensorChannel.close() // Close the channel when no longer needed
    }
}
