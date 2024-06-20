package com.wrongcode.captionwizard.di

import android.content.Context
import androidx.room.Room
import com.wrongcode.captionwizard.constants.Constants.Companion.DATABASE_NAME
import com.wrongcode.captionwizard.data.LocalDataSource
import com.wrongcode.captionwizard.data.Repository
import com.wrongcode.captionwizard.data.database.SubtitleDao
import com.wrongcode.captionwizard.data.database.SubtitleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): SubtitleDatabase {
        return Room.databaseBuilder(context, SubtitleDatabase::class.java, DATABASE_NAME).build()
    }

    @Singleton
    @Provides
    fun provideDao(database: SubtitleDatabase) : SubtitleDao{
        return database.subtitleDao()
    }
}