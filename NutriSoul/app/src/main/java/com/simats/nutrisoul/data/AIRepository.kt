package com.simats.nutrisoul.data

import android.content.Context
import android.graphics.Bitmap
import com.simats.nutrisoul.ObjectDetectorHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.vision.detector.Detection
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AIRepository @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun detectFood(imageBitmap: Bitmap): Result<List<Detection>> = withContext(Dispatchers.Default) {
        try {
            val detections = suspendCancellableCoroutine<List<Detection>> { continuation ->
                val objectDetectorListener = object : ObjectDetectorHelper.DetectorListener {
                    override fun onResults(
                        results: MutableList<Detection>?,
                        inferenceTime: Long,
                        imageHeight: Int,
                        imageWidth: Int
                    ) {
                        continuation.resume(results ?: emptyList())
                    }

                    override fun onError(error: String) {
                        continuation.resumeWithException(RuntimeException(error))
                    }
                }

                val objectDetectorHelper = ObjectDetectorHelper(
                    context = context,
                    objectDetectorListener = objectDetectorListener
                )

                continuation.invokeOnCancellation { objectDetectorHelper.clearObjectDetector() }

                objectDetectorHelper.detect(imageBitmap, 0)
            }
            Result.success(detections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
