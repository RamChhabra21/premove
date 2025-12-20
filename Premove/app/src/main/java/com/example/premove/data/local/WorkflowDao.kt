package com.example.premove.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.premove.model.WorkflowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao{
    @Query("SELECT * FROM workflows")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Insert
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Delete
    suspend fun deleteWorkflow(workflowId: WorkflowEntity)
}