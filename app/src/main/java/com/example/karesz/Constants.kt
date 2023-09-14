package com.example.karesz

import android.Manifest

object Constants {
    const val TAG = "camerax"
    const val REQUEST_CAMERACODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    const val MEDIA_PERMISSION = 101
    const val REQUEST_WRITE_PERMISSION = 102
}