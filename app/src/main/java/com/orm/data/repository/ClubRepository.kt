package com.orm.data.repository

import android.util.Log
import com.orm.data.api.ClubService
import com.orm.data.model.ClubMember
import com.orm.data.model.club.ClubApprove
import com.orm.data.model.club.Club
import com.orm.data.model.RequestMember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ClubRepository @Inject constructor(
    private val clubService: ClubService,
) {
    suspend fun getClubById(clubId: Int): Club? {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.getClubById(clubId).execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error getting club by id", e)
                null
            }
        }
    }
    
    suspend fun getClubs(keyword: String, isMyClub: Boolean): List<Club> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    clubService.getClubs(keyword = keyword, isMyClub = isMyClub).execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error getting clubs", e)
                emptyList()
            }
        }
    }

    suspend fun getAppliedClubs(): List<Club> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    clubService.getAppliedClubs().execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getMembers(clubId: Int): Map<String, List<ClubMember>> {
        return withContext(Dispatchers.IO) {
            val resultMap: MutableMap<String, List<ClubMember>> = mutableMapOf()
            try {
                val response = clubService.getMembers(clubId).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val members = responseBody?.get("members") ?: emptyList()
                    val applicants = responseBody?.get("applicants") ?: emptyList()
                    resultMap["members"] = members
                    resultMap["applicants"] = applicants
                } else {
                    resultMap["members"] = emptyList()
                    resultMap["applicants"] = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error getting members", e)
                resultMap["members"] = emptyList()
                resultMap["applicants"] = emptyList()
            }

            resultMap
        }
    }

    suspend fun approveClubs(approveClub: ClubApprove): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.approveClubs(approveClub).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error approving clubs", e)
                false
            }
        }
    }

    suspend fun leaveClubs(clubId: Int, userId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.leaveClubs(clubId, userId).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error leaving club", e)
                false
            }
        }
    }

    suspend fun applyClubs(requestMember: RequestMember): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.applyClubs(requestMember).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error applying to club", e)
                false
            }
        }
    }

    suspend fun createClubs(createClub: RequestBody, imgFile: MultipartBody.Part): Int {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.createClubs(createClub, imgFile).execute()
                if (response.isSuccessful) {
                    response.body() ?: -1
                } else {
                    -1
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error creating club", e)
                -1
            }
        }
    }

    suspend fun updateClubs(
        clubId: Int,
        createClub: RequestBody,
        imgFile: MultipartBody.Part
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.updateClubs(clubId, createClub, imgFile).execute()
                if (response.isSuccessful) {
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error creating club", e)
                false
            }
        }
    }

    suspend fun updateClubs(
        clubId: Int,
        createClub: RequestBody,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.updateClubs(clubId, createClub).execute()
                if (response.isSuccessful) {
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error creating club", e)
                false
            }
        }
    }

    suspend fun checkDuplicateClubs(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.checkDuplicateClubs(name).execute()
                if (response.isSuccessful) {
                    response.body() ?: false
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error checking duplicate clubs", e)
                false
            }
        }
    }

    suspend fun findClubsByMountain(mountainId: Int): List<Club> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    clubService.findClubsByMountain(mountainId = mountainId).execute()
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error getting clubs", e)
                emptyList()
            }
        }
    }

    suspend fun dropMember(clubId: Int, userId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.dropMember(clubId, userId).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error drop member", e)
                false
            }
        }
    }

    suspend fun cancelApply(clubId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = clubService.cancelApply(clubId).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}
