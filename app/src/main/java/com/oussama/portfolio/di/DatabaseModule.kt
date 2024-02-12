package com.oussama.portfolio.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oussama.portfolio.data.local.room.PortfolioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PortfolioDatabase =
        Room.databaseBuilder(context, PortfolioDatabase::class.java, "portfolio").fallbackToDestructiveMigration().build()
}