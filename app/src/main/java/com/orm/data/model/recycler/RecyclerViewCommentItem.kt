package com.orm.data.model.recycler

data class RecyclerViewCommentItem(
    val commentId: Int,
    val userId: Int,
    val userNickname: String,
    val content : String,
    val createdAt : String
)
