package com.foursum.echostrike.screens.game

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import com.foursum.echostrike.model.SensorData
import com.foursum.echostrike.util.SensorManagerUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.io.IOException

class GameScreenModel(private val context: Context) : ScreenModel {
    private val sensorManagerUtil = SensorManagerUtil(context)
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var fileWriter: FileWriter? = null

    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        try {
            val file = File(
                context.getExternalFilesDir(null),
                "sensor_data.csv"
            )
            fileWriter = FileWriter(file, false).apply {
                write(SensorData.getCsvHeader() + "\n")
            }
            _isRecording.value = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            fileWriter?.close()

            val file = File(context.getExternalFilesDir(null), "sensor_data.csv")
            val recordedData = file.readLines().drop(1) // Drop the header row

            val downsampledData = downsampleDataTo100Rows(recordedData)

            FileWriter(file, false).use { writer ->
                writer.write(SensorData.getCsvHeader() + "\n") // Write the header
                downsampledData.forEach { row ->
                    writer.write(row + "\n")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileWriter = null
            _isRecording.value = false
        }
    }

    private fun downsampleDataTo100Rows(data: List<String>): List<String> {
        val totalRows = data.size
        val step = if (totalRows > 100) totalRows / 100 else 1
        return data.filterIndexed { index, _ -> index % step == 0 }.take(100)
    }

    fun getSavedData() : List <SensorData> {
        val file = File(context.getExternalFilesDir(null), "sensor_data.csv")
        return csvFileToSensorData(file)
    }

    fun csvFileToSensorData(file: File): List<SensorData> {
        if (!file.exists()) return emptyList()

        try {
            // Read all lines and skip the header
            val lines = file.readLines().drop(1)

            return lines.mapNotNull { line ->
                try {
                    // Split the CSV row into values
                    val values = line.split(",")

                    // Check if we have enough values
                    if (values.size >= 6) {
                        // Parse the values into float arrays
                        val linearAcceleration = FloatArray(3) { i ->
                            values[i].toFloatOrNull() ?: 0f
                        }

                        val gyroscope = FloatArray(3) { i ->
                            values[i + 3].toFloatOrNull() ?: 0f
                        }

                        // Create magnetometer array if data exists
                        val magnetometer = if (values.size >= 9) {
                            FloatArray(3) { i ->
                                values[i + 6].toFloatOrNull() ?: 0f
                            }
                        } else {
                            FloatArray(3)
                        }

                        SensorData(linearAcceleration, gyroscope, magnetometer)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    init {
        sensorManagerUtil.setOnSensorDataChangedListener { updatedData ->
            _sensorData.value = updatedData
            if (isRecording.value) {
                try {
                    fileWriter?.write(updatedData.toCsvRow() + "\n")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDispose() {
        stopRecording()
        sensorManagerUtil.unregisterListeners()
    }
}