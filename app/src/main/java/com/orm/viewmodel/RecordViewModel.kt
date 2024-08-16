package com.orm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orm.data.model.Record
import com.orm.data.repository.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository
) : ViewModel() {
    private val _record = MutableLiveData<Record>()
    val record: LiveData<Record> get() = _record

    private val _records = MutableLiveData<List<Record>>()
    val records: LiveData<List<Record>> get() = _records

    private val _recordId = MutableLiveData<Long>()
    val recordId: LiveData<Long> get() = _recordId

    fun getRecord(id: Long) {
        viewModelScope.launch {
            val record = recordRepository.getRecord(id)
            _record.postValue(record)
        }
    }

    fun insertRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.insertRecord(record)
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            recordRepository.deleteAllRecords()
        }
    }

    fun getRecordCount() {
        viewModelScope.launch {
            val count = recordRepository.getRecordCount()
            _recordId.postValue(count.toLong())
        }
    }

    fun getAllRecords() {
        viewModelScope.launch {
            val records = recordRepository.getAllRecords()
            _records.postValue(records)
        }
    }
}