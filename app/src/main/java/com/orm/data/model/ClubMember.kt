package com.orm.data.model

import android.os.Parcelable
import com.orm.data.model.recycler.RecyclerViewButtonItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClubMember(
    val introduction: String = "",
    val userId: Int,
    val imgSrc: String?,
    val nickname: String,
    val clubId: Int? = null,
    val jointedAt: String? = null,
) : Parcelable {
    companion object {
        fun toRecyclerViewButtonItem(clubMember: ClubMember): RecyclerViewButtonItem {
            return RecyclerViewButtonItem(
                id = clubMember.userId,
                imageSrc = clubMember.imgSrc!!,
                title = clubMember.nickname,
                subTitle = clubMember.introduction,
                nickName = clubMember.nickname,
            )
        }
    }
}
