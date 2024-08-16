package com.orm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orm.data.model.Point
import com.orm.data.repository.PointRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointViewModel @Inject constructor(
    private val pointRepository: PointRepository,
) : ViewModel() {
    private val _points = MutableLiveData<List<Point>>()
    val points: LiveData<List<Point>> get() = _points

    fun getPoints(trailId: Int) {
        viewModelScope.launch {
            val points = pointRepository.getPointsByTrailId(trailId)
            _points.postValue(points)
        }
    }

    fun insertPoint(point: Point) {
        viewModelScope.launch {
            pointRepository.insertPoint(point)
        }
    }
}