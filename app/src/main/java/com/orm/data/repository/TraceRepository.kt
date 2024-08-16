package com.orm.data.repository

import android.content.Context
import android.util.Log
import com.orm.data.api.TraceService
import com.orm.data.local.dao.TraceDao
import com.orm.data.model.Trace
import com.orm.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TraceRepository @Inject constructor(
    private val traceDao: TraceDao,
    private val traceService: TraceService,
    @ApplicationContext private val context: Context,
) {
    suspend fun getAllTraces(): List<Trace> {
        return withContext(Dispatchers.IO) {
            traceDao.getAllTraces()
        }
    }

    suspend fun createTrace(trace: Trace): Long {
        return withContext(Dispatchers.IO) {
            val result = traceDao.insertTrace(trace)
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    traceService.createTrace(trace).execute()
                } catch (e: Exception) {
                    Log.e("TraceRepository", "Error creating trace", e)
                }
            }
            result
        }
    }

    suspend fun deleteTrace(trace: Trace): Int {
        return withContext(Dispatchers.IO) {
            val result = traceDao.deleteTrace(trace)
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    traceService.deleteTrace(trace.id!!).execute()
                } catch (e: Exception) {
                    Log.e("TraceRepository", "Error deleting trace", e)
                }
            }
            result
        }
    }

    suspend fun getTrace(id: Int): Trace {
        return withContext(Dispatchers.IO) {
            traceDao.getTrace(id)
        }
    }

    suspend fun updateTrace(trace: Trace): Long {
        return withContext(Dispatchers.IO) {
            val result = traceDao.insertTrace(trace)
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    traceService.updateTrace(trace).execute()
                } catch (e: Exception) {
                    Log.e("TraceRepository", "Error updating trace", e)
                }
            }
            result
        }
    }

    suspend fun measureComplete(trace: Trace): Long {
        return withContext(Dispatchers.IO) {
            val result = traceDao.insertTrace(trace)
            if (NetworkUtils.isNetworkAvailable(context)) {
                try {
                    traceService.measureComplete(trace).execute()
                } catch (e: Exception) {
                    Log.e("TraceRepository", "Error measuring complete trace", e)
                }
            }
            result
        }
    }

    suspend fun deleteAllTraces() {
        withContext(Dispatchers.IO) {
            traceDao.deleteAllTraces()
        }
    }
}
