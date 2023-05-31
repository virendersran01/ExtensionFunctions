package com.virtualstudios.extensionfunctions

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.http.POST

interface ApiService {
    @POST("/api/v1/users/refresh-token/")
    suspend fun refreshAccessToken(): TokenResponse
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Long
)

class AuthInterceptor(private val apiService: ApiService) : Interceptor {
    // Other interceptor configuration and properties

    private val sessionManager = SessionManager() // Replace with your session management class

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = sessionManager.getAccessToken()

        if (accessToken != null && sessionManager.isAccessTokenExpired()) {
            val refreshToken = sessionManager.getRefreshToken()

            // Make the token refresh request
            val refreshedToken = runBlocking {
                val response = apiService.refreshAccessToken()
                // Update the refreshed access token and its expiration time in the session
                sessionManager.updateAccessToken(response.accessToken, response.expiresIn)
                response.accessToken
            }

            if (refreshedToken != null) {
                // Create a new request with the refreshed access token
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $refreshedToken")
                    .build()

                // Retry the request with the new access token
                return chain.proceed(newRequest)
            }
        }

        // Add the access token to the request header
        val authorizedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authorizedRequest)
    }
}

class SessionManager {
    // Other session management properties and methods

    private var accessToken: String? = null
    private var accessTokenExpirationTime: Long? = null

    // Method to check if the access token has expired
    fun isAccessTokenExpired(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        return accessTokenExpirationTime != null && currentTimeMillis >= accessTokenExpirationTime!!
    }

    // Method to update the access token and its expiration time in the session
    fun updateAccessToken(token: String, expiresIn: Long) {
        accessToken = token
        accessTokenExpirationTime = System.currentTimeMillis() + expiresIn * 1000 // Convert expiresIn to milliseconds
    }

    // Other session management methods
}

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(apiService))
    // Other OkHttpClient configuration
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("BASE_URL")
    .client(okHttpClient)
    // Other Retrofit configuration
    .build()