package com.foursum.echostrike.model

data class SensorData(
    val linearAcceleration: FloatArray = FloatArray(3),
    val gyroscope: FloatArray = FloatArray(3),
    val magnetometer: FloatArray = FloatArray(3)
) {
    fun formatValues(values: FloatArray): String {
        return "X = %.2f, Y = %.2f, Z = %.2f".format(values[0], values[1], values[2])
    }

    fun toCsvRow(): String {
        return "${linearAcceleration.joinToString(",")},${gyroscope.joinToString(",")}"
    }

    companion object {
        fun getCsvHeader(): String {
            return "LinearAccelerationX,LinearAccelerationY,LinearAccelerationZ,GyroscopeX,GyroscopeY,GyroscopeZ"
        }
    }
}
