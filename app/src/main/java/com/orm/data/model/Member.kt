package com.orm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "member")
data class Member(
    @PrimaryKey val id: Int,
    val nickname: String,
    val imgSrc: String,
    val clubId: Int,
    val joinedAt: String
)