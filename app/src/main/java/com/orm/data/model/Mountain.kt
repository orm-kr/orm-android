package com.orm.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orm.data.model.recycler.RecyclerViewBasicItem
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "mountain")
data class Mountain(
    @PrimaryKey val id: Int,
    val name: String,
    val code: Int,
    val address: String?,
    val imageSrc: String?,
    val desc: String?,
    val height: Double,
    val addressLatitude: Double,
    val addressLongitude: Double,
    val trails: List<Trail>? = emptyList(),
) : Parcelable {
    companion object {
        fun toRecyclerViewBasicItem(mountain: Mountain): RecyclerViewBasicItem {
            return RecyclerViewBasicItem(
                id = mountain.id,
                imageSrc = mountain.imageSrc ?: "",
                title = mountain.name,
                subTitle = mountain.address ?: "",
            )
        }
    }
}