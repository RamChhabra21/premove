package com.example.premove.di

import android.content.Context
import com.example.premove.data.local.AppDatabase
import com.example.premove.data.local.dao.EdgeDao
import com.example.premove.data.local.dao.EdgeRunDao
import com.example.premove.data.local.dao.NodeDao
import com.example.premove.data.local.dao.NodeRunDao
import com.example.premove.data.local.dao.WorkflowDao
import com.example.premove.data.local.dao.WorkflowRunDao
import com.example.premove.data.repository.EdgeRepository
import com.example.premove.data.repository.EdgeRunRepository
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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

    @Provides
    fun provideNodeDao(db: AppDatabase): NodeDao {
        return db.NodeDao()
    }

    @Provides
    fun provideEdgeDao(db: AppDatabase): EdgeDao {
        return db.EdgeDao()
    }
    @Provides
    fun provideNodeRunDao(db: AppDatabase): NodeRunDao {
        return db.NodeRunDao()
    }

    @Provides
    fun provideWorkflowRunDao(db: AppDatabase): WorkflowRunDao {
        return db.WorkflowRunDao()
    }

    @Provides
    fun provideEdgeRunDao(db: AppDatabase): EdgeRunDao {
        return db.EdgeRunDao()
    }

    @Provides
    @Singleton
    fun provideWorkflowRepository(workflowDao: WorkflowDao): WorkflowRepository {
        return WorkflowRepository(workflowDao)
    }

    @Provides
    @Singleton
    fun provideNodeRepository(nodeDao: NodeDao): NodeRepository {
        return NodeRepository(nodeDao)
    }

    @Provides
    @Singleton
    fun provideEdgeRepository(edgeDao: EdgeDao): EdgeRepository {
        return EdgeRepository(edgeDao)
    }

    @Provides
    @Singleton
    fun provideWorkflowRunRepository(workflowRunDao: WorkflowRunDao): WorkflowRunRepository {
        return WorkflowRunRepository(workflowRunDao)
    }

    @Provides
    @Singleton
    fun provideNodeRunRepository(nodeRunDao: NodeRunDao): NodeRunRepository {
        return NodeRunRepository(nodeRunDao)
    }

    @Provides
    @Singleton
    fun provideEdgeRunRepository(edgeRunDao: EdgeRunDao): EdgeRunRepository {
        return EdgeRunRepository(edgeRunDao)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
