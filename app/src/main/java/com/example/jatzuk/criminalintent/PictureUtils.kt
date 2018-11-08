package com.example.jatzuk.criminalintent

import android.graphics.Bitmap
import android.graphics.BitmapFactory

//import android.app.Activity
//import android.graphics.Point

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1
    if (srcWidth > destWidth || srcHeight > destHeight) {
        val widthScale = srcWidth / destWidth
        val heightScale = srcHeight / destHeight
        inSampleSize = Math.round(if (heightScale > widthScale) heightScale else widthScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize
    return BitmapFactory.decodeFile(path, options)
}

//fun getScaledBitmap(path: String, activity: Activity): Bitmap {
//    val size = Point()
//    activity.windowManager.defaultDisplay.getSize(size)
//    return getScaledBitmap(path, size.x, size.y)
//}
