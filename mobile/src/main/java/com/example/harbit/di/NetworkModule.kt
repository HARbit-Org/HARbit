package com.example.harbit.di

import com.example.harbit.data.remote.interceptor.AuthInterceptor
import com.example.harbit.data.remote.service.ActivityApiService
import com.example.harbit.data.remote.service.AuthApiService
import com.example.harbit.data.remote.service.BackendApiService
import com.example.harbit.data.remote.service.UserApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    // OkHttpClient WITHOUT auth interceptor - used only for auth endpoints
    @Provides
    @Singleton
    @javax.inject.Named("AuthClient")
    fun provideAuthOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // OkHttpClient WITH auth interceptor - used for all other endpoints
    @Provides
    @Singleton
    @javax.inject.Named("ApiClient")
    fun provideApiOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    // Retrofit for auth endpoints (no auth interceptor)
    @Provides
    @Singleton
    @javax.inject.Named("AuthRetrofit")
    fun provideAuthRetrofit(
        @javax.inject.Named("AuthClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.18.113:8000")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    // Retrofit for other endpoints (with auth interceptor)
    @Provides
    @Singleton
    @javax.inject.Named("ApiRetrofit")
    fun provideApiRetrofit(
        @javax.inject.Named("ApiClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.18.113:8000")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBackendApiService(
        @javax.inject.Named("ApiRetrofit") retrofit: Retrofit
    ): BackendApiService {
        return retrofit.create(BackendApiService::class.java)
    }
    
    @Provides
    @Singleton
    @javax.inject.Named("AuthApiService")
    fun provideAuthApiService(
        @javax.inject.Named("AuthRetrofit") retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    // Also provide non-named version for regular injection
    @Provides
    @Singleton
    fun provideAuthApiServiceUnnamed(
        @javax.inject.Named("AuthApiService") authApiService: AuthApiService
    ): AuthApiService {
        return authApiService
    }
    
    @Provides
    @Singleton
    fun provideUserApiService(
        @javax.inject.Named("ApiRetrofit") retrofit: Retrofit
    ): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideActivityApiService(
        @javax.inject.Named("ApiRetrofit") retrofit: Retrofit
    ): ActivityApiService {
        return retrofit.create(ActivityApiService::class.java)
    }
}
