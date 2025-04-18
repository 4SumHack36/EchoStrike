package com.foursum.helper.ui.screenmodel

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import com.foursum.helper.models.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.io.IOException

class SensorScreenModel(
    private val sharedPrefsManager: SharedPrefsManager,
    private val context: Context
) : ScreenModel {

    private val sensorManagerUtil = SensorManagerUtil(context)
    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private var fileWriter: FileWriter? = null

    fun toggleRecording() {
        if (_isRecording.value) { // Use the backing property to ensure state updates
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        try {
            val file = File(
                Res.savedFilesPath,
                savedFileName(context)
            ) // this is /storage/emulated/0/Android/data/com.kanhaji.sensormonitor/files/sensor_data.csv
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

            val file = File(Res.savedFilesPath, savedFileName(context))
            val recordedData = file.readLines().drop(1) // Drop the header row

            val downsampledData = downsampleDataTo100Rows(recordedData)

            FileWriter(file, false).use { writer ->
                writer.write(SensorData.getCsvHeader() + "\n") // Write the header
                downsampledData.forEach { row ->
                    writer.write(row + "\n")
                }
            }

            Res.n++
            sharedPrefsManager.saveInt("n", Res.n)
            Res.nextFileName = "${Res.currentPrefix}_${Res.n}.csv"
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
        super.onDispose()
        stopRecording()
        sensorManagerUtil.unregisterListeners()
    }
}
