package com.virtualstudios.extensionfunctions

import android.content.Context
import android.content.SharedPreferences
import com.google.android.datatransport.runtime.dagger.Module
import com.google.gson.annotations.SerializedName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

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


/**
 * Interceptor to add auth token to requests
 */
class AuthInterceptor2(context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader(
                "Authorization", "Bearer $it"
            )
        }
        return chain.proceed(requestBuilder.build())
    }
}

//-------------------------------------------------------------------------------------------

/**
 * Authenticator to get the RefreshToken if current token is expired
 */
class AuthenticateApi(context: Context) : Authenticator {
    private val sessionManager = SessionManager2(context)

    override fun authenticate(route: Route?, response: Response): Request? {
        val apiClient = Retrofit.Builder().baseUrl(ApiUrl.BASE_URL) // api base url
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
        return runBlocking {
            // call login api again for getting accessToken in runBlocking so that it will wait until api response come
            val apiResponse = apiClient.login(LoginBody("piash599@gmail.com", "p1234", "user"))
            if (apiResponse.isSuccess) {
                val accessToken = apiResponse.data?.accessToken
                accessToken?.let {
                    sessionManager.saveAuthToken(accessToken)
                }
                response.request.newBuilder().addHeader(
                    "Authorization", "Bearer $accessToken"
                ).build()
            } else {
                null
            }
        }
    }
}

interface ApiService {
    @POST(ApiUrl.USER_LOGIN)
    suspend fun login(@Body loginBody: LoginBody): BaseModel<LoginResponse>
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * Provides BaseUrl as string
     */
    @Singleton
    @Provides
    fun provideBaseURL(): String {
        return ApiUrl.BASE_URL
    }

    /**
     * Provides LoggingInterceptor for api information
     */
    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    /**
     * Provides Auth interceptor for access token
     */
    @Singleton
    @Provides
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    /**
     * Provides AuthenticateApi to check api is authenticate or not
     */
    @Singleton
    @Provides
    fun provideAuthenticateApi(@ApplicationContext context: Context): AuthenticateApi {
        return AuthenticateApi(context)
    }

    /**
     * Provides custom OkkHttp
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        authenticateApi: AuthenticateApi
    ): OkHttpClient {
        val okHttpClient = OkHttpClient().newBuilder()

        okHttpClient.callTimeout(40, TimeUnit.SECONDS)
        okHttpClient.connectTimeout(40, TimeUnit.SECONDS)
        okHttpClient.readTimeout(40, TimeUnit.SECONDS)
        okHttpClient.writeTimeout(40, TimeUnit.SECONDS)
        okHttpClient.addInterceptor(loggingInterceptor)
        okHttpClient.addInterceptor(authInterceptor)
        okHttpClient.authenticator(authenticateApi)
        okHttpClient.build()
        return okHttpClient.build()
    }
    /**
     * Provides converter factory for retrofit
     */
    @Singleton
    @Provides
    fun provideConverterFactory(): Converter.Factory {
        return GsonConverterFactory.create()
    }

    /**
     * Provides ApiServices client for Retrofit
     */
    @Singleton
    @Provides
    fun provideRetrofitClient(
        baseUrl: String, okHttpClient: OkHttpClient, converterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }
    /**
     * Provides Api Service using retrofit
     */
    @Singleton
    @Provides
    fun provideRestApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}

/**
 * Session manager to save and fetch data from SharedPreferences
 */
class SessionManager2 (context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Function to fetch auth token
     */
    fun clearAuthToken() {
        prefs.edit().clear().commit()
    }
}