package com.virtualstudios.extensionfunctions.remote

import com.virtualstudios.extensionfunctions.TokenResponse
import retrofit2.http.POST

interface ApiService {

    @POST("/api/v1/remote")
    suspend fun getRemoteData(): ApiResponse<Any>

}