package com.virtualstudios.extensionfunctions.di_hilt

import android.os.Build
import com.google.gson.GsonBuilder
import com.virtualstudios.extensionfunctions.BuildConfig
import com.virtualstudios.extensionfunctions.Constants.BASE_URL
import com.virtualstudios.extensionfunctions.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    @Singleton
    @Provides
    fun provideHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient
            .Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                connectTimeout(60, TimeUnit.SECONDS)
                addInterceptor(httpLoggingInterceptor)
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