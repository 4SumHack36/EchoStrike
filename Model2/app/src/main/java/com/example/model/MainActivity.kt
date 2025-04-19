package com.example.model

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.model.ui.theme.ModelTheme
import loadModelFile
import org.tensorflow.lite.Interpreter

class MainActivity : ComponentActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load the model
        try {
            val interpreter = Interpreter(loadModelFile(assets, "model.tflite"))

            val input = Array(1) { Array(100) { FloatArray(6) { 0f } } }
            val output = Array(1) { FloatArray(3) } // ✅ FIXED to match model output

            interpreter.run(input, output)

            val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            println("Predicted class: $predictedClass")
            Toast.makeText(this, "Predicted class: $predictedClass", Toast.LENGTH_LONG).show()


        } catch (e: Exception) {
            e.printStackTrace()
            println("Model error: ${e.localizedMessage}")
            Toast.makeText(this, "Model error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()

        }


    }
}
