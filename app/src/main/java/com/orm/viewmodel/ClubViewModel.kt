package com.orm.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.orm.data.local.dao.ClubDao
import com.orm.data.model.ClubMember
import com.orm.data.model.club.ClubApprove
import com.orm.data.model.club.Club
import com.orm.data.model.RequestMember
import com.orm.data.model.club.ClubCreate
import com.orm.data.repository.ClubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ClubViewModel @Inject constructor(
    private val clubRepository: ClubRepository,
    private val clubDao: ClubDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _club = MutableLiveData<Club?>()
    val club: LiveData<Club?> get() = _club

    private val _clubs = MutableLiveData<List<Club>>()
    val clubs: LiveData<List<Club>> get() = _clubs

    private val _members = MutableLiveData<Map<String, List<ClubMember>>>()
    val members: LiveData<Map<String, List<ClubMember>>> get() = _members

    private val _isOperationSuccessful = MutableLiveData<Boolean?>()
    val isOperationSuccessful: LiveData<Boolean?> get() = _isOperationSuccessful

    private val _isCreated = MutableLiveData<Boolean?>()
    val isCreated: LiveData<Boolean?> get() = _isCreated

    private val _clubId = MutableLiveData<Int?>()
    val clubId: LiveData<Int?> get() = _clubId

    private val _isReady = MutableLiveData<Boolean>()
    val isReady: LiveData<Boolean> get() = _isReady

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getClubById(clubId: Int) {
        viewModelScope.launch {
            val club = clubRepository.getClubById(clubId)
            _club.postValue(club)
        }
    }

    fun getClubs(keyword: String = "", isMyClub: Boolean = false) {
        viewModelScope.launch {
            _isReady.postValue(false)
            val clubs = clubRepository.getClubs(keyword, isMyClub)
            _clubs.postValue(clubs)
            _isReady.postValue(true)
        }
    }

    fun getAppliedClubs() {
        viewModelScope.launch {
            _isReady.postValue(false)
            val clubs = clubRepository.getAppliedClubs()
            _clubs.postValue(clubs)
            _isReady.postValue(true)
        }
    }

    fun getMembers(clubId: Int) {
        viewModelScope.launch {
            val members = clubRepository.getMembers(clubId)
            _members.postValue(members)
        }
    }

    fun approveClubs(approveClub: ClubApprove) {
        viewModelScope.launch {
            try {
                val success = clubRepository.approveClubs(approveClub)
                _isOperationSuccessful.postValue(success)
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun leaveClubs(clubId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val success = clubRepository.leaveClubs(clubId, userId)
                _isOperationSuccessful.postValue(success)
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun applyClubs(requestMember: RequestMember) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                val success = clubRepository.applyClubs(requestMember)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun createClubs(clubCreate: ClubCreate, imgFile: File?) {
        viewModelScope.launch {
            _isCreated.postValue(false)
            try {
                val createClubRequestBody = createClubRequestBody(clubCreate)
                val imgFilePart = createImagePart(imgFile)

                val clubId = clubRepository.createClubs(createClubRequestBody, imgFilePart)
                _clubId.postValue(clubId)
                _isCreated.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _clubId.postValue(null)
            }
        }
    }

    fun updateClubs(clubId: Int, clubCreate: ClubCreate, imgFile: File?) {
        viewModelScope.launch {
            _isCreated.postValue(null)
            try {
                val createClubRequestBody = createClubRequestBody(clubCreate)
                val imgFilePart = createImagePart(imgFile)
                val result = clubRepository.updateClubs(clubId, createClubRequestBody, imgFilePart)
                _isCreated.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
                _clubId.postValue(null)
                _isCreated.postValue(false)
            }
        }
    }

    fun updateClubs(clubId: Int, clubCreate: ClubCreate) {
        viewModelScope.launch {
            _isCreated.postValue(null)
            try {
                val createClubRequestBody = createClubRequestBody(clubCreate)
                val result = clubRepository.updateClubs(clubId, createClubRequestBody)
                _isCreated.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
                _clubId.postValue(null)
                _isCreated.postValue(false)
            }
        }
    }

    fun checkDuplicateClubs(clubName: String) {
        viewModelScope.launch {
            try {
                val isDuplicate = clubRepository.checkDuplicateClubs(clubName)
                _isOperationSuccessful.postValue(isDuplicate)
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun resetOperationStatus() {
        _isOperationSuccessful.value = null
    }

    private fun createClubRequestBody(clubCreate: ClubCreate): RequestBody {
        val gson = Gson()
        val clubJson = gson.toJson(clubCreate)
        return clubJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

    private fun createImagePart(file: File?): MultipartBody.Part {
        // Create an empty file if the input file is null or doesn't exist
        val actualFile = file?.takeIf { it.exists() } ?: createEmptyFile()
        val requestFile = actualFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData("imgFile", actualFile.name, requestFile)
    }

    private fun createEmptyFile(): File {
        val emptyFileName = "empty_image.jpg"
        val emptyFile = File(context.cacheDir, emptyFileName)
        if (!emptyFile.exists()) {
            emptyFile.createNewFile()
        }
        return emptyFile
    }

    fun findClubsByMountain(mountainId: Int) {
        viewModelScope.launch {
            val clubs = clubRepository.findClubsByMountain(mountainId)
            Log.e("findClubsByMountain", clubs.toString())
            _clubs.postValue(clubs)
        }
    }

    fun dropMember(clubId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val success = clubRepository.dropMember(clubId, userId)
                _isOperationSuccessful.postValue(success)
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun cancelApply(clubId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                val success = clubRepository.cancelApply(clubId)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}