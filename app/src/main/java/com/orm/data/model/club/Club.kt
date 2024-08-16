package com.orm.data.model.club

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orm.R
import com.orm.data.model.recycler.RecyclerViewBasicItem
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "club")
data class Club(
    @PrimaryKey val id: Int,
    val clubName: String,
    val description: String,
    val imgSrc: String?,
    val managerId: String,
    val managerName: String,
    val memberCount: String,
    val mountainId: String,
    val mountainName: String,
    val isApplied: Boolean,
    val isMember: Boolean,
    val applicantCount: Int,
) : Parcelable {
    companion object {
        fun toRecyclerViewBasicItem(club: Club): RecyclerViewBasicItem {
            return RecyclerViewBasicItem(
                id = club.id,
                imageSrc = club.imgSrc ?: "",
                title = club.clubName,
                subTitle = club.mountainName
            )
        }
    }
}