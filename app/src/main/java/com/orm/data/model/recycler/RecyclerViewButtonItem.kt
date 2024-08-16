package com.orm.data.model.recycler

data class RecyclerViewButtonItem(
    val id: Int? = 0,
    val imageSrc: String,
    val title: String,
    val subTitle: String? = null,
    val nickName: String? = null,
)
