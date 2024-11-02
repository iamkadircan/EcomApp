package com.example.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.core.database.ecomdatabase.EcomDatabase
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
    fun provideEcomDatabase(@ApplicationContext context: Context): EcomDatabase {
        return Room.databaseBuilder(
            context,
            EcomDatabase::class.java,
            "ecom_database"
        ).build()
    }

}