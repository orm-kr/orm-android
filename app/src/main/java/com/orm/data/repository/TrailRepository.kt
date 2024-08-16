package com.orm.data.repository

import com.orm.data.local.dao.TrailDao
import com.orm.data.model.Trail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrailRepository @Inject constructor(
    private val trailDao: TrailDao,
) {
    suspend fun getTrail(id: Int): Trail {
        return withContext(Dispatchers.IO) {
            trailDao.getTrail(id)
        }
    }

    suspend fun createTrail(trail: Trail): Long {
        return withContext(Dispatchers.IO) {
            trailDao.createTrail(trail)
        }
    }

    suspend fun deleteAllTrails() {
        withContext(Dispatchers.IO) {
            trailDao.deleteAllTrails()
        }
    }
}