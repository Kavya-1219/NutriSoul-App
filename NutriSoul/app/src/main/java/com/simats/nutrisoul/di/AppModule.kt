package com.simats.nutrisoul.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.gson.Gson
import com.simats.nutrisoul.data.*
import com.simats.nutrisoul.data.network.NutritionApiService
import com.simats.nutrisoul.data.network.UserApi
import com.simats.nutrisoul.data.network.NutriSoulApiService
import com.simats.nutrisoul.data.network.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFoodDao(database: AppDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideIntakeDao(database: AppDatabase): IntakeDao {
        return database.intakeDao()
    }

    @Provides
    fun provideCustomFoodDao(database: AppDatabase): CustomFoodDao {
        return database.customFoodDao()
    }
    
    @Provides
    fun provideStepsDao(database: AppDatabase): StepsDao {
        return database.stepsDao()
    }

    @Provides
    fun provideFoodLogDao(database: AppDatabase): FoodLogDao {
        return database.foodLogDao()
    }

    @Provides
    fun provideSleepDao(database: AppDatabase): SleepDao {
        return database.sleepDao()
    }

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideUserApi(sessionManager: SessionManager): UserApi {
        return ApiClient.create(sessionManager)
    }

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        userApi: UserApi,
        userDao: UserDao,
        sessionManager: SessionManager,
        gson: Gson
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(userApi, userDao, sessionManager, gson)
    }

    @Provides
    @Singleton
    fun provideNutritionApiService(): NutritionApiService {
        return ApiClient.apiService
    }

    @Provides
    @Singleton
    fun provideNutriSoulApiService(sessionManager: SessionManager): NutriSoulApiService {
        // Assuming ApiClient can create this too, or we need a way to create it.
        // Let's check ApiClient first to be sure.
        return ApiClient.createNutriSoulApi(sessionManager)
    }
}
