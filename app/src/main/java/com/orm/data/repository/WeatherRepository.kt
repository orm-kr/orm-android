package com.orm.data.repository

import com.orm.data.api.WeatherService
import com.orm.data.model.weather.Weather
import com.orm.data.model.weather.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService
) {
    suspend fun getWeather(lat: Double, lon: Double): Weather? {
        return withContext(Dispatchers.IO) {
            val response = weatherService.getWeather(lat = lat, lon = lon).execute()
            if (response.isSuccessful) {
                val closestPastTime = getClosestPastTime()
                val weatherDataList = response.body()?.list?.filter { it.dt_txt.endsWith(closestPastTime) } ?: emptyList()
                if (weatherDataList.size == 3) {
                    parseWeatherData(weatherDataList)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun getClosestPastTime(): String {
        val currentTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalTime()
        return when {
            currentTime.isBefore(LocalTime.of(3, 0)) -> "00:00:00"
            currentTime.isBefore(LocalTime.of(6, 0)) -> "03:00:00"
            currentTime.isBefore(LocalTime.of(9, 0)) -> "06:00:00"
            currentTime.isBefore(LocalTime.of(12, 0)) -> "09:00:00"
            currentTime.isBefore(LocalTime.of(15, 0)) -> "12:00:00"
            currentTime.isBefore(LocalTime.of(18, 0)) -> "15:00:00"
            currentTime.isBefore(LocalTime.of(21, 0)) -> "18:00:00"
            else -> "21:00:00"
        }
    }

    private fun parseWeatherData(weatherDataList: List<WeatherData>): Weather {
        val data1 = weatherDataList[0]
        val data2 = weatherDataList[1]
        val data3 = weatherDataList[2]

        return Weather(
            data1 = data1.dt_txt,
            tmp1 = data1.main.temp.toInt(),
            description1 = data1.weather.first().description,
            icon1 = getIconUrl(data1.weather.first().icon),
            data2 = data2.dt_txt,
            tmp2 = data2.main.temp.toInt(),
            description2 = data2.weather.first().description,
            icon2 = getIconUrl(data2.weather.first().icon),
            data3 = data3.dt_txt,
            tmp3 = data3.main.temp.toInt(),
            description3 = data3.weather.first().description,
            icon3 = getIconUrl(data3.weather.first().icon)
        )
    }

    private fun getIconUrl(iconCode: String): String {
        return "https://openweathermap.org/img/wn/$iconCode.png"
    }

}