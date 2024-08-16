package com.orm.data.model.board

data class BoardCreate(
    val clubId: Int,
    val title: String,
    val content: String,
    val imgSrc: List<String>,
)
