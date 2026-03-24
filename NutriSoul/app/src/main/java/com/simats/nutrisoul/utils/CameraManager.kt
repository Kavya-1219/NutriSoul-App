package com.simats.nutrisoul.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.simats.nutrisoul.CameraController

class CameraManager(private val context: Context) : CameraController {

    private var cameraController: LifecycleCameraController? = null

    fun getController(): LifecycleCameraController {
        if (cameraController == null) {
            cameraController = LifecycleCameraController(context).apply {
                setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
            }
        }
        return cameraController!!
    }

    override fun takePicture(onBitmap: (Bitmap) -> Unit) {
        cameraController?.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    onBitmap(rotatedBitmap)
                    image.close()
                }
            }
        )
    }
}
