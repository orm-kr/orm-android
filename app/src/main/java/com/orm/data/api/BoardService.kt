package com.orm.data.api

import com.orm.data.model.board.Board
import com.orm.data.model.board.BoardList
import com.orm.data.model.board.Comment
import com.orm.data.model.board.CreateComment
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface BoardService {

    @GET("clubs/boards")
    fun getBoardList(
        @Query("clubId") clubId: Int,
    ): Call<List<BoardList>>

    @GET("clubs/boards/{boardId}")
    fun getBoards(
        @Path("boardId") boardId : Int,
    ): Call<Board>

    @Multipart
    @POST("clubs/boards/create")
    fun createBoards(
        @Part("createBoard") createBoard: RequestBody,
        @Part imgFile: List<MultipartBody.Part>
    ): Call<Unit>

    @Multipart
    @PATCH("clubs/boards/update/{boardId}")
    fun updateBoards(
        @Path("boardId") boardId : Int,
        @Part("updateBoard") createBoard: RequestBody,
        @Part imgFile: List<MultipartBody.Part>
    ): Call<Board>

    @DELETE("clubs/boards/delete")
    fun deleteBoards(
        @Query("boardId") boardId: Int
    ): Call<Unit>

    @POST("clubs/boards/{boardId}/comments/create")
    fun createComments(
        @Path("boardId") boardId : Int,
        @Body content : CreateComment,
    ): Call<Comment>

    @PATCH("clubs/boards/{boardId}/comments/update/{commentId}")
    fun updateComments(
        @Path("boardId") boardId : Int,
        @Path("commentId") commentId : Int,
        @Body content : CreateComment
    ): Call<Comment>

    @DELETE("clubs/boards/{boardId}/comments/delete")
    fun deleteComments(
        @Path("boardId") boardId : Int,
        @Query("commentId") commentId : Int,
    ): Call<Unit>
}