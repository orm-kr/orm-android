package com.orm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orm.data.model.Trail
import com.orm.data.repository.TrailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrailViewModel @Inject constructor(
    private val trailRepository: TrailRepository,
) : ViewModel() {
    private val _trail = MutableLiveData<Trail>()
    val trail: LiveData<Trail> get() = _trail

    fun getTrail(id: Int) {
        viewModelScope.launch {
            val points = trailRepository.getTrail(id)
            _trail.postValue(points)
        }
    }

    fun createTrail(trail: Trail) {
        viewModelScope.launch {
            trailRepository.createTrail(trail)
        }
    }

    fun deleteAllTrails() {
        viewModelScope.launch {
            trailRepository.deleteAllTrails()
        }
    }
}