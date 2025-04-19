import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

fun loadModelFile(assetManager: AssetManager, filename: String): MappedByteBuffer {
    val fileDescriptor: AssetFileDescriptor = assetManager.openFd(filename)
    val inputStream = fileDescriptor.createInputStream()
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}
