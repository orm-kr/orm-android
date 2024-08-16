package com.orm.di

import android.content.Context
import com.orm.BuildConfig
import com.orm.data.api.BoardService
import com.orm.data.api.ClubService
import com.orm.data.api.MountainService
import com.orm.data.api.TraceService
import com.orm.data.api.UserService
import com.orm.data.api.WeatherService
import com.orm.data.local.PreferencesKeys
import com.orm.util.NetworkUtils
import com.orm.util.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.inject.Named
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(appInterceptor: AppInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(appInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAppInterceptor(@ApplicationContext context: Context): AppInterceptor {
        return AppInterceptor(context)
    }

    class AppInterceptor @Inject constructor(
        @ApplicationContext private val context: Context,
    ) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
            if (!NetworkUtils.isNetworkAvailable(context)) {
                throw IOException("No network available")
            }

            val token = context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.tokenString] ?: ""
            }.first()

            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }
    }

    @Provides
    @Singleton
    fun provideClubService(retrofit: Retrofit): ClubService {
        return retrofit.create(ClubService::class.java)
    }

    @Provides
    @Singleton
    fun provideMountainService(retrofit: Retrofit): MountainService {
        return retrofit.create(MountainService::class.java)
    }

    @Provides
    @Singleton
    fun provideTraceService(retrofit: Retrofit): TraceService {
        return retrofit.create(TraceService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideBoardService(retrofit: Retrofit): BoardService {
        return retrofit.create(BoardService::class.java)
    }

    @Provides
    @Singleton
    @Named("weatherRetrofit")
    fun provideWeatherRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherService(@Named("weatherRetrofit") retrofit: Retrofit): WeatherService {
        return retrofit.create(WeatherService::class.java)
    }

}
