package com.orm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orm.data.model.club.Club
import com.orm.data.model.weather.Weather
import com.orm.data.model.weather.WeatherData
import com.orm.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    private val _weather = MutableLiveData<Weather?>()
    val weather: LiveData<Weather?> get() = _weather

    fun getWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            val weatherItem = weatherRepository.getWeather(lat, lon)
            _weather.postValue(weatherItem)
            Log.d("WeatherViewModel", "Received weather data: $weatherItem")
        }
    }
}