package com.orm.data.model

data class User(
    val userId: String,
    val imageSrc: String,
    val nickname: String,
    var gender: String = "male",
    var age: Int = 23,
    var level: Int = 1,
    var pushNotificationsEnabled: Boolean = true
)