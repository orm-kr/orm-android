package com.orm.viewmodel

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.orm.R
import com.orm.data.model.board.Board
import com.orm.data.model.board.BoardCreate
import com.orm.data.model.board.BoardList
import com.orm.data.model.board.Comment
import com.orm.data.model.board.CreateComment
import com.orm.data.model.club.Club
import com.orm.data.repository.BoardRepository
import com.orm.ui.board.BoardDetailActivity
import com.orm.util.resizeImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLDecoder
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardRepository: BoardRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _boardList = MutableLiveData<List<BoardList>>()
    val boardList: LiveData<List<BoardList>> get() = _boardList

    private val _board = MutableLiveData<Board?>()
    val board: LiveData<Board?> get() = _board

    private val _isOperationSuccessful = MutableLiveData<Boolean?>()
    val isOperationSuccessful: LiveData<Boolean?> get() = _isOperationSuccessful

    private val _comment = MutableLiveData<Comment?>()
    val comment: LiveData<Comment?> get() = _comment


    private val imageFileParts = mutableListOf<MultipartBody.Part>()
    private val imgSrc = mutableListOf<String>()

    fun getBoardList(clubId: Int) {
        viewModelScope.launch {
            _boardList.value = emptyList()
            val boardList = boardRepository.getBoardList(clubId)
            Log.d("getboards", "response1 : $boardList")
            _boardList.value = boardList
        }
    }

    fun getBoards(boardId: Int) {
        viewModelScope.launch {
            val board = boardRepository.getBoards(boardId)
            Log.d("getboards", "response1 : $board")
            _board.postValue(board)
        }
    }

    fun createBoards(
        clubId: Int,
        title: String,
        content: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                imageFileParts.clear()
                val processedContent = processContent(content)
                val boardCreate = BoardCreate(clubId, title, processedContent, imgSrc)
                val createBoardRequestBody = createBoardRequestBody(boardCreate)
                val success = boardRepository.createBoards(createBoardRequestBody, imageFileParts)

                if (success) {
                    onSuccess()
                    _isOperationSuccessful.value = success
                } else {
                    onFailure(Exception("create fail"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure(e)
            }
        }
    }

    fun updateBoards(
        clubId: Int,
        title: String,
        content: String,
        boardId: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val processedContent = processContent(content)
                val boardCreate = BoardCreate(clubId, title, processedContent, imgSrc)
                val createBoardRequestBody = createBoardRequestBody(boardCreate)
                val board = boardRepository.updateBoards(boardId, createBoardRequestBody, imageFileParts)
                _board.postValue(board)
                if (board != null) {
                    onSuccess()
                } else {
                    onFailure(Exception("update fail"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure(e)
            }
        }
    }

    fun deleteBoards(boardId: Int) {
        viewModelScope.launch {
            try {
                val success = boardRepository.deleteBoards(boardId)
                _isOperationSuccessful.postValue(success)
                Log.d("BoardViewModel1", "success : $success")
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun createComments(boardId: Int, content: String) {
        viewModelScope.launch {
            try {
                val createComment = CreateComment(content)
                val newComment = boardRepository.createComments(boardId, createComment)
                _comment.postValue(newComment)
                _isOperationSuccessful.postValue(newComment != null)
                Log.d("BoardViewModel1", "New comment created: $newComment")
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun updateComments(boardId: Int, commentId: Int, content: String) {
        viewModelScope.launch {
            try {
                val createComment = CreateComment(content)
                val updatedComment =
                    boardRepository.updateComments(boardId, commentId, createComment)
                _comment.postValue(updatedComment)
                _isOperationSuccessful.postValue(updatedComment != null)
                Log.d("BoardViewModel1", "Comment updated: $updatedComment")
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }

    fun deleteComments(boardId: Int, commentId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BoardViewModel1", "deleteComments :")
                val success = boardRepository.deleteComments(boardId, commentId)
                _isOperationSuccessful.postValue(success)
                Log.d("BoardViewModel1", "success : $success")
            } catch (e: Exception) {
                e.printStackTrace()
                _isOperationSuccessful.postValue(false)
            }
        }
    }


    private fun createBoardRequestBody(clubCreate: BoardCreate): RequestBody {
        val gson = Gson()
        val clubJson = gson.toJson(clubCreate)
        return clubJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    }

//    private suspend fun matchContent(content: String) {
//        val pattern = Pattern.compile("<img src=\"(.*?)\"")
//        val matcher = pattern.matcher(content)
//
//        Log.d("vm", "detail22 vm : $content")
//        while (matcher.find()) {
//            val contentUrl = matcher.group(1) // content:// 경로
//            if (contentUrl != null) {
//                if (contentUrl.startsWith("https://")) {
//                    imgSrc.add(contentUrl)
//                } else if (contentUrl.startsWith("content://")) {
//                    handleImageSelection(Uri.parse(contentUrl))
//                }
//            }
//        }
//    }

    private suspend fun processContent(content: String): String {
        val pattern = Pattern.compile("<img src=\"(.*?)\"")
        val matcher = pattern.matcher(content)
        var processedContent = content

        while (matcher.find()) {
            val contentUrl = matcher.group(1)
            if (contentUrl != null) {
                if (contentUrl.startsWith("https://")) {
                    imgSrc.add(contentUrl)
                } else if (contentUrl.startsWith("content://")) {
                    handleImageSelection(Uri.parse(contentUrl))
                }
            }
        }
        return processedContent
    }

    private suspend fun handleImageSelection(uri: Uri) {
        val resizedFile = resizeImageSuspend(context, uri)
        Log.d("BoardEditActivity", "file987 uri: $uri")
        Log.d("BoardEditActivity", "file987 resizedfile: $resizedFile")
        if (resizedFile != null) {
            addImageToUploadList(resizedFile)
        } else {
            Log.e("handleImageSelection", "Image resizing failed.")
        }
    }

    private suspend fun resizeImageSuspend(context: Context, uri: Uri): File? {
        return suspendCancellableCoroutine { continuation ->
            resizeImage(context, uri) { resizedFile ->
                if (resizedFile != null) {
                    val uriString = uri.toString()
                    val decodedUriString = URLDecoder.decode(uriString, "UTF-8") // URI 디코딩
                    val imageId = decodedUriString.substringAfterLast("/") // 디코딩된 파일 이름 추출
                    val newFile = File(resizedFile.parent, "$imageId.jpg") // 디코딩된 파일 이름 사용
                    resizedFile.renameTo(newFile)
                    continuation.resume(newFile) {}
                } else {
                    continuation.resume(null) {}
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }

    private fun addImageToUploadList(file: File) {
        Log.d("img", "img123 name : ${file.name}")
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        Log.d("img", "img123 name : $requestFile")
        val body = MultipartBody.Part.createFormData("imgFile", file.name, requestFile)
        imageFileParts.add(body)

        Log.d("BoardEditActivity", "file987 : ${file.name}")
        Log.d("BoardEditActivity", "file987 size: ${file.length()}")
    }

}