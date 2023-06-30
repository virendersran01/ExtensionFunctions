package com.virtualstudios.extensionfunctions.di_hilt

import android.content.Context
import android.os.Build
import com.google.gson.GsonBuilder
import com.virtualstudios.extensionfunctions.BuildConfig
import com.virtualstudios.extensionfunctions.Constants.BASE_URL
import com.virtualstudios.extensionfunctions.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import logDebug
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    @Provides
    fun providesOkhttp():OkHttpClient{
        val okhttp = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logger = HttpLoggingInterceptor()
            logger.setLevel(HttpLoggingInterceptor.Level.BODY)

            okhttp.addInterceptor(logger)
        }
        return okhttp.build()
    }

    @Singleton
    @Provides
    fun provideHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient
            .Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
                if (BuildConfig.DEBUG) {
                    addInterceptor(httpLoggingInterceptor)
                }
                addInterceptor { chain ->
                    var request: Request = chain.request()
                    val url: HttpUrl = request.url.newBuilder().apply {
                        addQueryParameter("os_type", "android")
                        addQueryParameter("app_version_code", BuildConfig.VERSION_CODE.toString())
                        addQueryParameter("app_version_name", BuildConfig.VERSION_NAME)
                        addQueryParameter("os_version", Build.VERSION.SDK_INT.toString())
                        addQueryParameter("phone_manufacturer", Build.MANUFACTURER)
                        addQueryParameter("model", Build.MODEL)
                    }.build()
                    request = request.newBuilder().url(url).build()
                    chain.proceed(request)
                }
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .create()
        )

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

   /* @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                setLevel(HttpLoggingInterceptor.Level.NONE)
            }
        }
    }

    @Singleton
    @Provides
    fun provideHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient
            .Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
                addInterceptor { chain ->
                    var request: Request = chain.request()
                    val url: HttpUrl = request.url.newBuilder().apply {
                        addQueryParameter("os_type", "android")
                        addQueryParameter("app_version_code", BuildConfig.VERSION_CODE.toString())
                        addQueryParameter("app_version_name", BuildConfig.VERSION_NAME)
                        addQueryParameter("os_version", Build.VERSION.SDK_INT.toString())
                        addQueryParameter("phone_manufacturer", Build.MANUFACTURER)
                        addQueryParameter("model", Build.MODEL)
                    }.build()
                    request = request.newBuilder().url(url).build()
                    chain.proceed(request)
                }
                addInterceptor(authInterceptor)
                addInterceptor(httpLoggingInterceptor)
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .create()
        )

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Singleton
    @Provides
    fun provideAuthInterceptor(
        appUserPreferences: AppUserPreferences
    ): Interceptor {
        return Interceptor { chain ->
            val request =
                chain.request().newBuilder().apply {
                    if (appUserPreferences.getAccessToken() != null) {
                        header("Authorization", appUserPreferences.getAccessToken()!!)
                    }
                }.build()
            chain.proceed(request)
        }
    }*/



/*    private var numRequests: Int = 0
    private var firstRequestDate: LocalDateTime? = null


    private const val MAX_REQUESTS_NUM = 40
    private const val MINUTES_MAX_REQUESTS = 1

    @Provides
    @Singleton
    fun provideCoinGeckoApiService(
        @ApplicationContext context: Context
    ): ApiService {
        val cacheSize = 10 * 1024 * 1024L // 10MB
        val cache = Cache(context.cacheDir, cacheSize)
        //val maxAgeSeconds = 60 * 4 // Caches the responses for n seconds

        val okHttpClient = OkHttpClient.Builder()
            *//*
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())

            Timber.d("${response.headers()}")

            response.newBuilder()
                .header("Cache-Control", "public, max-age=$maxAgeSeconds")
                .removeHeader("Pragma")
                .build()


        }

             *//*
            .addNetworkInterceptor { chain ->
                // This interceptor tracks the number of network requests performed in a given time range
                // to limit them in case they exceed the maximum number set.
                logDebug(message = "NetworkInterceptor: ${chain.request()}")

                firstRequestDate?.let {
                    numRequests++

                    if (numRequests >= MAX_REQUESTS_NUM) {
                        val minutesFromFirstRequest = it.minutesBetween(LocalDateTime.now())
                        logDebug(message = "$chain, $numRequests, $minutesFromFirstRequest")

                        if (minutesFromFirstRequest >= MINUTES_MAX_REQUESTS) {
                            initializeRequestsCounter()
                        } else {
                            // In case the maximum number of requests is reached, the request
                            // is blocked by launching the following exception
                            throw Exception(

                            )
                        }
                    }

                } ?: kotlin.run {
                    initializeRequestsCounter()
                }

                val response = chain.proceed(chain.request())



                when(response.code()) {
                    429, 503 -> {
                        // In case of a service unavailable response code from the server,
                        // the requests counter is set to the maximum value so that the next requests
                        // are temporarily blocked by this interceptor before being forwarded to the server
                        // to avoid overcharging it.
                        setRequestsCounterToExceedingValues()
                        response
                    }
                    else -> response
                }
            }
            .cache(cache)
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }

    private fun initializeRequestsCounter() {
        numRequests = 1
        firstRequestDate = LocalDateTime.now()
    }

    private fun setRequestsCounterToExceedingValues() {
        numRequests = MAX_REQUESTS_NUM
        firstRequestDate = LocalDateTime.now()
    }*/


//    private val retrofit = Retrofit.Builder().apply {
//        baseUrl(BASE_URL)
//        addConverterFactory(GsonConverterFactory.create())
//        addLogger(this, okHttpClient())
//    }.build()
//
//
//    private fun addLogger(builder: Retrofit.Builder, clientBuilder: OkHttpClient.Builder) {
//        if (BuildConfig.DEBUG) {
//            val interceptor = HttpLoggingInterceptor()
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//            val client: OkHttpClient = clientBuilder.addInterceptor(interceptor).build()
//            builder.client(client)
//        } else {
//            builder.client(clientBuilder.build())
//        }
//    }
//
//    private fun okHttpClient() : OkHttpClient.Builder{
//        val builder = OkHttpClient.Builder()
//        builder.connectTimeout(2, TimeUnit.MINUTES)
//        builder.readTimeout(2, TimeUnit.MINUTES)
//        builder.writeTimeout(2, TimeUnit.MINUTES)
//        builder.interceptors().add(Interceptor { chain ->
//            var request: Request = chain.request()
//            val url: HttpUrl = request.url.newBuilder().apply {
//                addQueryParameter("os_type", "android")
//                addQueryParameter("app_version_code", BuildConfig.VERSION_CODE.toString())
//                addQueryParameter("app_version_name", BuildConfig.VERSION_NAME)
//                addQueryParameter("os_version", Build.VERSION.SDK_INT.toString())
//                addQueryParameter("phone_manufacturer", Build.MANUFACTURER)
//                addQueryParameter("model", Build.MODEL)
//            }.build()
//            request = request.newBuilder().url(url).build()
//            chain.proceed(request)
//        })
//        return builder
//    }

}