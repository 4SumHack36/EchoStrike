package com.foursum.helper.util

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@SuppressLint("SdCardPath")
object Res {
    var currentPrefix by mutableStateOf("<NULL>")
    var n by mutableIntStateOf(1)
    //currentPrefix + "_" + n
    var nextFileName by mutableStateOf("${currentPrefix}_$n")
    var savedFilesPath by mutableStateOf("/sdcard/SensorData")
}
