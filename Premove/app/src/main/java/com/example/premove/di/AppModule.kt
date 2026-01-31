package com.example.premove.di

import android.content.Context
import com.example.premove.data.local.AppDatabase
import com.example.premove.data.local.WorkflowDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return AppDatabase.getDatabase(appContext)
    }

    @Provides
    fun provideWorkflowDao(db: AppDatabase): WorkflowDao {
        return db.WorkflowDao()
    }
}
