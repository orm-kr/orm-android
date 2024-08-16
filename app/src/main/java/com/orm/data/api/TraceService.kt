package com.orm.data.api

import com.orm.data.model.Trace
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface TraceService {
    // 발자국 생성
    @POST("trace/create")
    fun createTrace(
        @Body trace: Trace
    ): Call<Trace>

    // 발자국 수정 (측정 전)
    @PATCH("trace/update")
    fun updateTrace(
        @Body trace: Trace
    ): Call<Unit>

    // 발자국 수정 (측정 후 - 이미지)
    @Multipart
    @PATCH("trace/update/images/{traceId}")
    fun updateImages(
        @Path("traceId") traceId: Int,
        @Part images: List<MultipartBody.Part>
    ): Call<Unit>

    // 발자국 측정 완료
    @PATCH("trace/measure-complete")
    fun measureComplete(
        @Body trace: Trace
    ): Call<Unit>

    // 발자국 삭제
    @DELETE("trace/{traceId}")
    fun deleteTrace(
        @Path("traceId") traceId: Int
    ): Call<Unit>
}