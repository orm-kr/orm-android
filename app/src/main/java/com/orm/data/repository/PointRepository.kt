package com.orm.data.repository

import com.orm.data.local.dao.PointDao
import com.orm.data.model.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PointRepository @Inject constructor(
    private val pointDao: PointDao,
) {
    suspend fun getPointsByTrailId(trailId: Int): List<Point> {
        return withContext(Dispatchers.IO) {
            pointDao.getPointsByTrailId(trailId)
        }
    }

    suspend fun insertPoint(point: Point): Long {
        return withContext(Dispatchers.IO) {
            pointDao.insertPoint(point)
        }
    }
}