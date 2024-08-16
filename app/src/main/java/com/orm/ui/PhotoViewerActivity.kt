package com.orm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alexvasilkov.gestures.views.GestureImageView
import com.bumptech.glide.Glide
import com.orm.R

class PhotoViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        val photoView = findViewById<GestureImageView>(R.id.photoView)
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        val imageResource = if (imageUrl.isNullOrEmpty()) {
            R.drawable.img_orm_1000
        } else {
            imageUrl
        }

        Glide.with(this)
            .load(imageResource)
            .into(photoView)
    }
}