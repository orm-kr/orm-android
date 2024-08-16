package com.orm.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "trail")
data class Trail(
    @PrimaryKey val id: Int,
    val peekLatitude: Double,
    val peekLongitude: Double,
    val startLatitude: Double,
    val startLongitude: Double,
    val trailDetails: List<Point>,
    val heuristic: Double,
    val distance: Double,
    val time: Int,
    val height: Double,
) : Parcelable