package com.orm.data.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orm.data.model.recycler.RecyclerViewNumberItem
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
@Entity
data class Trace(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int?,
    val title: String,
    val hikingDate: String?,
    val mountainId: Int,
    val mountainName: String?,
    val trailId: Int? = -1,
    var hikingStartedAt: Long? = null,
    var hikingEndedAt: Long? = null,
    var hikingDistance: Double? = 0.0,
    var maxHeight: Double? = 0.0,
    val coordinates: List<Point>?,
    var recordId: Long? = null,
    val imgPath: String? = null,
) : Parcelable {
    companion object {
        fun toRecyclerViewNumberItem(trace: Trace): RecyclerViewNumberItem {
            return RecyclerViewNumberItem(
                id = trace.localId,
                imageSrc = trace.imgPath ?: "",
                title = trace.title,
                subTitle = trace.mountainName ?: "",
                date = trace.hikingDate.toString(),
                btnText = if (trace.recordId != null) "측정완료" else "측정전"
            )
        }
    }
}