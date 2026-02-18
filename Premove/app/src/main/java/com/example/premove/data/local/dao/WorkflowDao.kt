package com.example.premove.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.premove.data.local.entity.WorkflowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao{
    @Query("Select * from workflows")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Query("Select * from workflows where isEnabled = 1")
    fun getActiveWorkflows(): List<WorkflowEntity>

    @Query("Select * from workflows where id=:workflowId")
    suspend fun getWorkflowById(workflowId: String): WorkflowEntity

    @Insert
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Query("Delete from workflows where id = :workflowId")
    suspend fun deleteWorkflowById(workflowId: String)

    @Query("Update workflows set isEnabled = not isEnabled where id = :workflowId")
    suspend fun toggleWorkflow(workflowId: String)
}