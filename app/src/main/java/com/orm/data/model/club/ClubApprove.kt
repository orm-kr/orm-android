package com.orm.data.model.club

data class ClubApprove(
    val clubId: Int,
    val userId: Int,
    val isApproved: Boolean
)
