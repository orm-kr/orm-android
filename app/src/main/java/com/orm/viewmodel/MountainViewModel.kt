package com.orm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.orm.data.model.Mountain
import com.orm.data.model.Point
import com.orm.data.model.Trail
import com.orm.data.repository.MountainRepository
import com.orm.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MountainViewModel @Inject constructor(
    private val mountainRepository: MountainRepository,
) : ViewModel() {

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> get() = _mountain

    private val _mountains = MutableLiveData<List<Mountain>?>()
    val mountains: LiveData<List<Mountain>?> get() = _mountains

    private val _points = MutableLiveData<List<Point>?>()
    val points: LiveData<List<Point>?> get() = _points

    private val _trail = MutableLiveData<Trail?>()
    val trail: LiveData<Trail?> get() = _trail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _recommndId = MutableLiveData<Int>()
    val recommendId: LiveData<Int> get() = _recommndId

    private var isDataLoaded = false

    fun fetchMountainByName(name: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val mountains = mountainRepository.getMountainByName(name)
            _mountains.postValue(mountains)
            _isLoading.postValue(false)
        }
    }

    fun fetchMountainById(id: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val mountain = mountainRepository.getMountainById(id, true)
            _mountain.postValue(mountain)
            _isLoading.postValue(false)
        }
    }

    fun fetchMountainById(id: Int, trailContaining: Boolean) {
        viewModelScope.launch {
            val mountain = mountainRepository.getMountainById(id, trailContaining)
            _mountain.postValue(mountain)
        }
    }

    fun fetchMountainsTop() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val mountains = mountainRepository.getMountainsTop()
            _mountains.postValue(mountains)
            _isLoading.postValue(false)
        }
    }

    fun fetchMountainsAll() {
        if (!isDataLoaded || _mountains.value.isNullOrEmpty()) {
            _isLoading.postValue(true)
            viewModelScope.launch {
                try {
                    val result = mountainRepository.getMountainsAll()
                    _mountains.postValue(result)
                    isDataLoaded = true
                } catch (e: Exception) {
                    Log.e("MountainViewModel", "Error fetching mountains", e)
                } finally {
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun fetchTrailById(trailId: Int) {
        viewModelScope.launch {
            val trail = mountainRepository.getTrailById(trailId)
            if (trail != null) {
                _points.postValue(trail.trailDetails)
            } else {
                _points.postValue(emptyList())
            }
        }
    }

    fun clearMountainData() {
        _mountain.value = null
    }

    fun getMountainsRecommend() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val recommendId = mountainRepository.getMountainsRecommend()
            _recommndId.postValue(recommendId ?: 1)
            val mountain = mountainRepository.getMountainById(recommendId ?: 1, false)
            _mountain.postValue(mountain)
            _isLoading.postValue(false)
        }
    }
}