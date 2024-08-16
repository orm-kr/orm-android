package com.orm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coordinate: List<Point>? = null,
)
