package com.simats.nutrisoul

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine

object LabelUtil {
    suspend fun labelImage(context: Context, uri: Uri): List<String> =
        suspendCancellableCoroutine { cont ->
            try {
                val image = InputImage.fromFilePath(context, uri)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        cont.resume(labels.sortedByDescending { it.confidence }.map { it.text }, null)
                    }
                    .addOnFailureListener {
                        cont.resume(emptyList(), null)
                    }
            } catch (e: Exception) {
                cont.resume(emptyList(), null)
            }
        }
}
