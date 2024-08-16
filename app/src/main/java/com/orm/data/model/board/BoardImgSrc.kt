package com.orm.data.model.board

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoardImgSrc(
    val imgSrc: String,
): Parcelable
