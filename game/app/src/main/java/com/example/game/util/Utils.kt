package com.example.game.util


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import kotlin.collections.forEach
import kotlin.io.copyTo
import kotlin.io.use
import kotlin.text.substringAfter
import kotlin.text.substringBefore


var text by mutableStateOf("")


fun getSharedFileName(intent: Intent): String? {
    val action = intent.action
    val type = intent.type

    if (Intent.ACTION_SEND == action && type != null) {
        val sharedFileUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (sharedFileUri != null) {
            val fileName = File(sharedFileUri.path as String).name
            return fileName
        }
    }
    return null
}

fun createFilesSavedDirectory() {
    val directory = File(Res.savedFilesPath)
    if (!directory.exists()) {
        directory.mkdirs()
    }
}

fun getDateFormatted(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS")
    return simpleDateFormat.format(System.currentTimeMillis())
}

fun savedFileName(context: Context): String {
    val sharedPrefsManager = SharedPrefsManager(context)

    // Get the file name prefix or use default "saved"
    val prefix = sharedPrefsManager.getString("fileName", "<NULL>")
    Res.currentPrefix = prefix

    // Get current counter value and increment for next time
    val counter = sharedPrefsManager.getInt("n", 1)
    Res.n = counter
    Res.nextFileName
    val newCounter = counter + 1
    sharedPrefsManager.saveInt("fileCounter", newCounter)

    val fileName = "${prefix}_${counter}.csv"

    return fileName
}

fun getMimeTypeFromUri(context: Context, uri: Uri): Pair<String, String> {

    val mimeType = context.contentResolver.getType(uri)
    val type = mimeType?.substringBefore("/")
    val extension = mimeType?.substringAfter("/")
    return Pair(type!!, extension!!)
//    return context.contentResolver.getType(uri)
}

suspend fun copyFileToSdCard(context: Context, fileUri: Uri) {
    withContext(Dispatchers.IO) {
        val fileName = savedFileName(context)
        val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
        val outputFile = File(Res.savedFilesPath, fileName)

        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
                try {
                    KToast.show(context, "Copied $text")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    // Close the app after the copy task is finished
//    (context as? ComponentActivity)?.finishAffinity()
}

suspend fun copyFilesToSdCard(context: Context, fileUris: List<Uri>) {
    withContext(Dispatchers.IO) {
        fileUris.forEach { fileUri ->
            val filename = savedFileName(context)
            val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
            val outputFile = File(Res.savedFilesPath, filename)

            inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                    try {
                        KToast.show(context, "Copied $text")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    // Close the app after the copy task is finished
    (context as? ComponentActivity)?.finishAffinity()
}