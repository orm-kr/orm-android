package com.orm.data.model.recycler

data class RecyclerViewBoardItem(
    val id: Int=0,
    val title : String,
    val userNickname : String,
    val commentCount : Int,
    val hit : Int,
    val createdAt : String
)
