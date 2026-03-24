package com.simats.nutrisoul

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object VisionUtil {
    suspend fun labelImage(context: Context, uri: Uri): List<String> =
        suspendCancellableCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        val result = labels.filter { it.confidence > 0.6f }.map { it.text }
                        cont.resume(result)
                    }
                    .addOnFailureListener {
                        cont.resume(emptyList())
                    }
            } catch (e: Exception) {
                cont.resume(emptyList())
            }
        }
}
