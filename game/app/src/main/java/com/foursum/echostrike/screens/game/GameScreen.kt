package com.foursum.echostrike.screens.game

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import loadModelFile
import org.tensorflow.lite.Interpreter
import androidx.compose.runtime.LaunchedEffect
import com.foursum.echostrike.model.SensorData
import com.foursum.echostrike.util.KToast

data class GameScreen(
    private val context: Context,
): Screen {
    @Composable
    override fun Content() {
        val model = rememberScreenModel { GameScreenModel(context) }
        val data by model.sensorData.collectAsState()
        val isRecording by model.isRecording.collectAsState()
        val ctx = LocalContext.current
        val assetManager = ctx.assets

//        GameComponent(data, isRecording, onToggleRecording = { model.toggleRecording() })
//
//        LaunchedEffect(Unit) {
//            try {
//                val interpreter = Interpreter(loadModelFile(assetManager, "model.tflite"))
//
//                val savedData = model.getSavedData()
//
//                // Prepare input: [1][100][6]
//                val input = Array(1) {
//                    Array(100) { i ->
//                        val sensor = savedData.getOrNull(i) ?: SensorData()
//                        floatArrayOf(
//                            sensor.linearAcceleration[0],
//                            sensor.linearAcceleration[1],
//                            sensor.linearAcceleration[2],
//                            sensor.gyroscope[0],
//                            sensor.gyroscope[1],
//                            sensor.gyroscope[2]
//                        )
//                    }
//                }
//
//                val output = Array(1) { FloatArray(3) } // 3 = number of classes
//
//                interpreter.run(input, output)
//
//                val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1
//                Toast.makeText(ctx, "Predicted class: $predictedClass", Toast.LENGTH_LONG).show()
//                println("Predicted class: $predictedClass")
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(ctx, "Model error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
//                println("Model error: ${e.localizedMessage}")
//            }
//        }
        //manish
        GameComponent(data, isRecording, onToggleRecording = {
            model.toggleRecording { sensorDataList ->
                // Convert sensorDataList to input tensor
                val input = Array(1) { Array(100) { FloatArray(6) } }
                sensorDataList.take(100).forEachIndexed { i, sensorData ->
                    input[0][i] = floatArrayOf(
                        sensorData.linearAcceleration[0],
                        sensorData.linearAcceleration[1],
                        sensorData.linearAcceleration[2],
                        sensorData.gyroscope[0],
                        sensorData.gyroscope[1],
                        sensorData.gyroscope[2]
                    )
                }

                try {
                    val interpreter = Interpreter(loadModelFile(context.assets, "model.tflite"))
                    val output = Array(1) { FloatArray(3) }
                    interpreter.run(input, output)

                    val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1
//                    Toast.makeText(context, "Predicted class: $predictedClass", Toast.LENGTH_LONG).show()
                    KToast.show(context,"Predicted class: $predictedClass", Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    e.printStackTrace()
                    KToast.show(context,"Model error: ${e.localizedMessage}", Toast.LENGTH_LONG)
//                    Toast.makeText(context, "Model error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        })

    }

}