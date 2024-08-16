package com.orm.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.orm.R
import com.orm.data.model.weather.Weather
import com.orm.databinding.FragmentGraphBinding
import com.orm.databinding.FragmentHomeCardBinding
import com.orm.databinding.FragmentWeatherBinding


class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

    fun updateWeather(weather: Weather) {
        // 날짜 1
        Glide.with(this).load(weather.icon1).into(binding.imageViewDay1)
        binding.textViewTemp1.text = "오늘 : ${weather.tmp1}°C"

        // 날짜 2
        Glide.with(this).load(weather.icon2).into(binding.imageViewDay2)
        binding.textViewTemp2.text = "내일 : ${weather.tmp2}°C"

        // 날짜 3
        Glide.with(this).load(weather.icon3).into(binding.imageViewDay3)
        binding.textViewTemp3.text = "모레 : ${weather.tmp3}°C"
    }
}