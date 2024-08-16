package com.orm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orm.data.local.dao.TraceDao
import com.orm.data.model.Trace
import com.orm.data.repository.TraceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TraceViewModel @Inject constructor(
    private val traceRepository: TraceRepository,
) : ViewModel() {

    private val _traces = MutableLiveData<List<Trace>>()
    val traces: LiveData<List<Trace>> get() = _traces

    private val _trace = MutableLiveData<Trace>()
    val trace: LiveData<Trace> get() = _trace

    private val _traceCreated = MutableLiveData<Boolean>()
    val traceCreated: LiveData<Boolean> get() = _traceCreated

    private val _traceCreatedNormal = MutableLiveData<Boolean>()
    val traceCreatedNormal: LiveData<Boolean> get() = _traceCreatedNormal

    private val _traceCreatedAbnormal = MutableLiveData<Boolean>()
    val traceCreatedAbnormal: LiveData<Boolean> get() = _traceCreatedAbnormal


    fun getTraces() {
        viewModelScope.launch {
            val traces = traceRepository.getAllTraces()
            _traces.postValue(traces)
        }
    }

    fun getTrace(id: Int) {
        viewModelScope.launch {
            val trace = traceRepository.getTrace(id)
            _trace.postValue(trace)
        }
    }

    fun createTrace(trace: Trace) {
        viewModelScope.launch {
            _traceCreated.postValue(false)
            _traceCreatedNormal.postValue(false)
            _traceCreatedAbnormal.postValue(false)

            traceRepository.createTrace(trace)
            _traceCreated.postValue(true)
            _traceCreatedNormal.postValue(true)
            _traceCreatedAbnormal.postValue(false)
        }
    }

    fun deleteTrace(trace: Trace) {
        viewModelScope.launch {
            traceRepository.deleteTrace(trace)
            getTraces()
        }
    }

    fun deleteAllTraces() {
        viewModelScope.launch {
            traceRepository.deleteAllTraces()
        }
    }
}
