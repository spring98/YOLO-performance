package com.spring.yolo.performance

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("submit")
    fun createData(@Body performances: Performances): Call<Void>
}