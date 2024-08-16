package com.orm.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun localDateTimeToLong(dateTime: LocalDateTime): Long {
    return dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()
}

fun timestampToTimeString(timestamp: Long, type: String = "HH:mm:ss"): String {
    val dateTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.of("Asia/Seoul"))
        .toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern(type)
    return dateTime.format(formatter)
}


fun getTimeDifferenceFormatted(startTimestamp: Long, endTimestamp: Long): String {
    val startInstant = Instant.ofEpochMilli(startTimestamp)
    val endInstant = Instant.ofEpochMilli(endTimestamp)

    val duration = Duration.between(startInstant, endInstant)

    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}