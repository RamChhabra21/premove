package com.example.premove.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.premove.data.local.entity.NodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao{
    @Query("Select * from nodes")
    fun getAllNodes(): Flow<List<NodeEntity>>

    @Query(value = "Select * from nodes where workflowId = :workflowId")
    fun getNodesByWorkflowId(workflowId: String): Flow<List<NodeEntity>>

    @Query("Select * from nodes where id=:nodeId")
    fun getNodeById(nodeId: String): Flow<NodeEntity>

    @Insert
    suspend fun insertNode(node: NodeEntity)

    @Update
    suspend fun updateNode(node: NodeEntity)

    @Query(value = "Update nodes set x = :x, y = :y where id = :nodeId")
    suspend fun updateNodePosition(nodeId: Int, x: Float, y: Float)

    @Query("Delete from nodes where id = :nodeId")
    suspend fun deleteNodeById(nodeId: String)
}