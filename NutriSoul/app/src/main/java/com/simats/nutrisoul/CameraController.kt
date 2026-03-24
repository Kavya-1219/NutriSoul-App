package com.simats.nutrisoul

import android.graphics.Bitmap

interface CameraController {
    fun takePicture(onBitmap: (Bitmap) -> Unit)
}
