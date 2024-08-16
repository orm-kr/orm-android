package com.orm.data.repository

import android.util.Log
import com.orm.data.api.BoardService
import com.orm.data.model.board.Board
import com.orm.data.model.board.BoardList
import com.orm.data.model.board.Comment
import com.orm.data.model.board.CreateComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BoardRepository @Inject constructor(
    private val boardService: BoardService,
) {

    suspend fun getBoardList(clubId: Int): List<BoardList>{
        return withContext(Dispatchers.IO){
            try{
                val response = boardService.getBoardList(clubId).execute()
                Log.d("boardRepository", "response1 : $response")
                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    emptyList()
                }
            }catch(e: Exception){
                emptyList()
            }

        }
    }

    suspend fun getBoards(boardId: Int): Board? {
        return withContext(Dispatchers.IO) {
            try {
                val response = boardService.getBoards(boardId).execute()
                Log.d("boardRepository", "response1 : $response")
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("ClubRepository", "Error getBoards", e)
                null
            }
        }
    }

    suspend fun createBoards(createBoard: RequestBody, imgFile: List<MultipartBody.Part>): Boolean  {
        return withContext(Dispatchers.IO) {
            try {
                val response = boardService.createBoards(createBoard, imgFile).execute()
                Log.d("BoardRepository", " BoardViewModel1 : $response ")
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("BoardRepository", "Error createBoards", e)
                false
            }
        }
    }

    suspend fun updateBoards(boardId:Int, createBoard: RequestBody, imgFile: List<MultipartBody.Part>): Board?  {
        return withContext(Dispatchers.IO) {

            try {
                val response = boardService.updateBoards(boardId, createBoard, imgFile).execute()
                Log.d("BoardRepository", " BoardViewModel1 : $response ")
                response.body()
            } catch (e: Exception) {
                Log.e("BoardRepository", "Error createBoards", e)
                null
            }
        }
    }

    suspend fun deleteBoards(boardId: Int): Boolean{
        return withContext(Dispatchers.IO) {
            try{
                val response = boardService.deleteBoards(boardId).execute()
                Log.d("BoardRepository", " BoardViewModel1 : deleteBoards $response ")
                response.isSuccessful
            }catch (e: Exception) {
                Log.e("BoardRepository", "Error deleteBoards", e)
                false
            }
        }
    }

    suspend fun createComments(boardId: Int, content: CreateComment): Comment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = boardService.createComments(boardId, content).execute()
                Log.d("BoardRepository", "BoardViewModel1 : createComments $response")
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("BoardRepository", "Error creating comment", e)
                null
            }
        }
    }

    suspend fun updateComments(boardId: Int,commentId: Int, content: CreateComment): Comment? {
        return  withContext(Dispatchers.IO){
            try {
                val response = boardService.updateComments(boardId, commentId, content).execute()
                Log.d("BoardRepository", "BoardViewModel1 : createComments $response")
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("BoardRepository", "Error creating comment", e)
                null
            }
        }
    }

    suspend fun deleteComments(boardId: Int,commentId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = boardService.deleteComments(boardId, commentId).execute()
                Log.d("BoardRepository", " BoardViewModel1 : deleteComments $response ")
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("BoardRepository", "Error deleteComments", e)
                false
            }
        }
    }
}