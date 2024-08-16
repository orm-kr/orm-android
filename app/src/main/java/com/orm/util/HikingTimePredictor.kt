package com.orm.util

import android.content.Context
import android.util.Log
import com.orm.data.model.Trail
import com.orm.data.model.User
import com.orm.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HikingTimePredictor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository
) {
    private var interpreter: Interpreter
    private var mean: FloatArray
    private var scale: FloatArray

    init {
        val modelPath = "hiking_time_prediction_model.tflite"
        interpreter = Interpreter(loadModelFile(context, modelPath))
        mean = loadArrayFromTxt(context, "scaler_mean.txt")
        scale = loadArrayFromTxt(context, "scaler_scale.txt")
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadArrayFromTxt(context: Context, assetName: String): FloatArray {
        val inputStream = context.assets.open(assetName)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        val content = String(buffer)
        val values = content.split("\n")
        return values.filter { it.isNotEmpty() }.map { it.toFloat() }.toFloatArray()
    }

    private fun normalizeInput(input: FloatArray): FloatArray {
        val normalized = FloatArray(input.size)
        for (i in input.indices) {
            normalized[i] = (input[i] - mean[i]) / scale[i]
        }
        return normalized
    }

    private fun predict(input: FloatArray): Float {
        val normalizedInput = normalizeInput(input)
        val inputBuffer =
            ByteBuffer.allocateDirect(normalizedInput.size * 4).order(ByteOrder.nativeOrder())
        normalizedInput.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()

        val output = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, output)
        output.rewind()
        return output.float + 10f
    }

    suspend fun predictTrail(trail: Trail): Float = withContext(Dispatchers.IO) {
        val altitude = trail.height.toFloat()
        val distance = trail.distance.toFloat() * 1000
        val averageTime = trail.time.toFloat()

        val user: User = userRepository.getUserInfo()!!
        val gender = if (user.gender == "male") 1f else 0f
        val level = (3 - user.level.toFloat()) % 3
        val age = user.age.toFloat()

        val input = floatArrayOf(altitude, gender, age, distance, level, averageTime)
        Log.d("AITEST", "${altitude} ${gender} ${age} ${distance} ${level} ${averageTime}")
        predict(input)
    }
}
