package com.example.premove.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.premove.data.local.entity.EdgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EdgeDao {
    @Insert
    suspend fun insertEdge(edge: EdgeEntity)

    @Query("SELECT * FROM EdgeEntity WHERE workflowId = :workflowId")
    fun getEdgesByWorkflowId(workflowId: String): Flow<List<EdgeEntity>>
}