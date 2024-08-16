package com.orm.data.repository

import android.util.Log
import com.orm.data.local.dao.RecordDao
import com.orm.data.model.Record
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordRepository @Inject constructor(
    private val recordDao: RecordDao,
) {
    suspend fun getRecord(id: Long): Record {
        return withContext(Dispatchers.IO) {
            recordDao.getRecord(id)
        }
    }

    suspend fun deleteRecord(record: Record): Int {
        return withContext(Dispatchers.IO) {
            recordDao.deleteRecord(record)
        }
    }

    suspend fun insertRecord(record: Record): Long {
        return withContext(Dispatchers.IO) {
            recordDao.insertRecord(record)
        }
    }

    suspend fun deleteAllRecords() {
        withContext(Dispatchers.IO) {
            recordDao.deleteAllRecords()
        }
    }

    suspend fun getRecordCount(): Int {
        return withContext(Dispatchers.IO) {
            recordDao.getRecordCount()
        }
    }

    suspend fun getAllRecords(): List<Record> {
        return withContext(Dispatchers.IO) {
            recordDao.getAllRecords()
        }
    }
}