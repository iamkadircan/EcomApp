package com.example.core.datastore.di



import android.content.Context
import com.example.core.datastore.EcomDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatastoreModule {

    @Provides
    @Singleton
    fun provideEcomDatastore(@ApplicationContext context: Context): EcomDataStore =
        EcomDataStore(context)
}