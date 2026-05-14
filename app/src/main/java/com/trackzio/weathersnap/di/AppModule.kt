package com.trackzio.weathersnap.di
import android.content.Context
import androidx.room.Room
import com.trackzio.weathersnap.data.local.CityCacheDao
import com.trackzio.weathersnap.data.local.WeatherReportDao
import com.trackzio.weathersnap.data.local.WeatherSnapDatabase
import com.trackzio.weathersnap.data.remote.api.GeocodingApi
import com.trackzio.weathersnap.data.remote.api.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("geocoding")
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGeocodingApi(@Named("geocoding") retrofit: Retrofit): GeocodingApi =
        retrofit.create(GeocodingApi::class.java)

    @Provides
    @Singleton
    fun provideWeatherApi(@Named("weather") retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WeatherSnapDatabase =
        Room.databaseBuilder(context, WeatherSnapDatabase::class.java, "weathersnap.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideWeatherReportDao(database: WeatherSnapDatabase): WeatherReportDao =
        database.weatherReportDao()

    @Provides
    @Singleton
    fun provideCityCacheDao(database: WeatherSnapDatabase): CityCacheDao =
        database.cityCacheDao()
}