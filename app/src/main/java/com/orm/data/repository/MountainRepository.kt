package com.orm.data.repository

import android.util.Log
import com.orm.data.api.MountainService
import com.orm.data.model.Mountain
import com.orm.data.model.Trail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MountainRepository @Inject constructor(
    private val mountainService: MountainService,
) {
    suspend fun getMountainByName(name: String): List<Mountain> {
        return withContext(Dispatchers.IO) {
            try {
                val response = mountainService.searchMountains(name).execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("MountainRepository", "Error getting mountain by name", e)
                emptyList()
            }
        }
    }

    suspend fun getMountainById(id: Int, trailContaining: Boolean): Mountain? {
        return withContext(Dispatchers.IO) {
            try {
                val response = mountainService.getMountainById(id, trailContaining).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("MountainRepository", "Error getting mountain by ID", e)
                null
            }
        }
    }

    suspend fun getTrailById(trailId: Int): Trail? {
        return withContext(Dispatchers.IO) {
            try {
                val response = mountainService.getTrail(trailId).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("MountainRepository", "Error getting trail by ID", e)
                null
            }
        }
    }

    suspend fun getMountainsTop(): List<Mountain>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = mountainService.getMountainsTop().execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("MountainRepository", "Error getting top mountains", e)
                emptyList()
            }
        }
    }

    suspend fun getMountainsAll(): List<Mountain>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = mountainService.getMountainsAll().execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("MountainRepository", "Error getting all mountains", e)
                emptyList()
            }
        }
    }

    suspend fun getMountainsRecommend(): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val response = mountainService.getMountainsRecommend().execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    1
                }
            } catch (e: Exception) {
                Log.e("MountainRepository", "Error getting recommend mountains", e)
                1
            }
        }
    }
}
