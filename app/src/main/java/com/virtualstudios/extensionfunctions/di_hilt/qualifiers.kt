package com.virtualstudios.extensionfunctions.di_hilt

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class BaseUrl

@Qualifier
annotation class AuthInterceptor

@Qualifier
annotation class TokenInterceptor

@Qualifier
annotation class LangInterceptor

@Qualifier
annotation class ApiRetrofit

@Qualifier
annotation class DirectionApiRetrofit