package com.orm.data.api

import com.orm.BuildConfig
import com.orm.data.model.weather.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("forecast")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = BuildConfig.WEATHER_API_KEY,
        @Query("cnt") cnt: Int = 24,
        @Query("units") units: String = "metric",
    ): Call<WeatherResponse>
}