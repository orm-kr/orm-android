package com.orm.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.orm.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun uriToFile(uri: Uri, contentResolver: ContentResolver): File {
    val file = File.createTempFile("temp_image", ".jpg")
    val inputStream: InputStream? = contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(file)

    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    return file
}

fun resizeImage(context: Context, uri: Uri, callback: (File?) -> Unit) {
    val requestOptions = RequestOptions()
        .override(800, 600)

    Glide.with(context)
        .asBitmap()
        .load(uri)
        .error(R.drawable.img_orm_1000)
        .placeholder(R.drawable.img_orm_1000)
        .apply(requestOptions)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val resizedFile = File(context.filesDir, "resized_image.jpg")
                FileOutputStream(resizedFile).use { out ->
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                callback(resizedFile)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}