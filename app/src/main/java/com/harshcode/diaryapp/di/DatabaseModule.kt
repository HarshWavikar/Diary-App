package com.harshcode.diaryapp.di

import android.content.Context
import androidx.room.Room
import com.harshcode.diaryapp.connectivity.NetworkConnectivityObserver
import com.harshcode.diaryapp.data.database.ImagesDatabase
import com.harshcode.diaryapp.util.Constants.IMAGES_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ImagesDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ImagesDatabase::class.java,
            name = IMAGES_DATABASE
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFirstDAO(database: ImagesDatabase) = database.imageToUploadDAO()

    @Provides
    @Singleton
    fun provideSecondDAO(database: ImagesDatabase) = database.imageToDeleteDAO()

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ) = NetworkConnectivityObserver(context = context)
}