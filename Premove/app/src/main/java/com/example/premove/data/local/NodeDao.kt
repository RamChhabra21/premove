package com.example.premove.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.premove.model.NodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao{
    @Query("Select * from nodes")
    fun getAllNodes(): Flow<List<NodeEntity>>

    @Query("Select * from nodes where id=:nodeId")
    fun getNodeById(nodeId: String): Flow<NodeEntity>

    @Insert
    suspend fun insertNode(node: NodeEntity)

    @Update
    suspend fun updateNode(node: NodeEntity)

    @Query("Delete from nodes where id = :nodeId")
    suspend fun deleteNodeById(nodeId: String)
}