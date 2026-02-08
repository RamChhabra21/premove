package com.example.premove.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.premove.data.local.dao.EdgeDao
import com.example.premove.data.local.dao.NodeDao
import com.example.premove.data.local.dao.WorkflowDao
import com.example.premove.data.local.entity.EdgeEntity
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.WorkflowEntity

@Database(entities = [WorkflowEntity::class, NodeEntity::class, EdgeEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun WorkflowDao(): WorkflowDao

    abstract fun NodeDao(): NodeDao

    abstract fun EdgeDao(): EdgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "premover_db"
                ).fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}