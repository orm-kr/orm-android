package com.orm.data.api

import com.orm.data.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserService {
    @GET("users/login/kakao/auth")
    fun loginKakao(@Query("code") code: String): Call<User>

    @GET("users/login/auto")
    fun loginAuto(): Call<User>

    @DELETE("users/leave")
    fun deleteUser(): Call<Unit>

    @POST("users/login/register-firebase")
    fun registerFirebaseToken(@Body firebaseToken: String): Call<Unit>
}