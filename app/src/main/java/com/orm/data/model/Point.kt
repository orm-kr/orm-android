package com.orm.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sqrt

@Parcelize
@Entity(tableName = "point")
data class Point(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val trailId: Int? = null,
    val x: Double,
    val y: Double,
    val d: Int? = null,
    val time: Long? = null,
    val altitude: Double? = null,
) : Parcelable {
    companion object {
        fun getDistance(p1: Point, p2: Point): Double {
            val R = 6371e3

            val φ1 = Math.toRadians(p1.x)
            val φ2 = Math.toRadians(p2.x)
            val λ1 = Math.toRadians(p1.y)
            val λ2 = Math.toRadians(p2.y)

            val x = (λ2 - λ1) * cos((φ1 + φ2) / 2)
            val y = φ2 - φ1
            val flatDistance = R * sqrt(x * x + y * y)

            var altitudeDifference = 0.0
            if (!(p1.altitude == null || p2.altitude == null)) {
                altitudeDifference = p2.altitude - p1.altitude
            }

            return sqrt(flatDistance * flatDistance + altitudeDifference * altitudeDifference)
        }
    }
}