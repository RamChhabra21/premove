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
    @Query("Select * from workflows")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Query("Select * from workflows where id=:workflowId")
    fun getWorkflowById(workflowId: String): Flow<WorkflowEntity>

    @Insert
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Query("Delete from workflows where id = :workflowId")
    suspend fun deleteWorkflowById(workflowId: String)

    @Query("Update workflows set isEnabled = not isEnabled where id = :workflowId")
    suspend fun toggleWorkflow(workflowId: String)
}