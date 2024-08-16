package com.orm.data.api

import com.orm.data.model.Mountain
import com.orm.data.model.Trail
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MountainService {
    // 등산로 ID로 검색
    @GET("mountains/trail/{trailId}")
    fun getTrail(@Path("trailId") trailId: Int): Call<Trail>

    // 산 ID로 불러오기
    @GET("mountains/{mountainID}")
    fun getMountainById(
        @Path("mountainID") mountainID: Int,
        @Query("trailContaining") trailContaining: Boolean
    ): Call<Mountain>

    // 산 이름으로 검색
    @GET("mountains/search")
    fun searchMountains(@Query("name") name: String): Call<List<Mountain>>

    // 100대 명산 불러오기
    @GET("mountains/top")
    fun getMountainsTop(): Call<List<Mountain>>

    @GET("mountains/all")
    fun getMountainsAll(): Call<List<Mountain>>

    @GET("mountains/recommend")
    fun getMountainsRecommend(): Call<Int>

}