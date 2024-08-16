package com.orm.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.orm.R
import java.io.File

object BindingAdapters {
    @BindingAdapter("imageSrc")
    @JvmStatic
    fun loadImage(view: ImageView, imageUrl: String?) {
        Glide.with(view.context)
            .load(imageUrl ?: R.drawable.img_orm_1000) // imageUrl이 null일 경우 기본 이미지 로드
            .placeholder(R.drawable.img_orm_1000)
            .error(R.drawable.img_orm_1000)
            .centerCrop()
            .into(view)
    }

    @BindingAdapter("imageUri")
    @JvmStatic
    fun setImageUri(imageView: ImageView, imgPath: String?) {
        if (!imgPath.isNullOrEmpty()) {
            val file = File(imgPath)
            if (file.exists()) {
                Glide.with(imageView.context)
                    .load(file)
                    .placeholder(R.drawable.img_orm_1000)
                    .error(R.drawable.img_orm_1000)
                    .centerCrop()
                    .into(imageView)
            } else {
                // 파일이 존재하지 않는 경우 기본 이미지 로드
                Glide.with(imageView.context)
                    .load(R.drawable.img_orm_1000)
                    .into(imageView)
            }
        } else {
            // imgPath가 null 또는 empty일 경우 기본 이미지 로드
            Glide.with(imageView.context)
                .load(R.drawable.img_orm_1000)
                .into(imageView)
        }
    }
}
