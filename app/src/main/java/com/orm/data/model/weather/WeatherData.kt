package com.orm.data.model.weather

data class WeatherData(
    val main: Temperature,
    val weather: List<WeatherDescription>,
    val dt_txt: String
)