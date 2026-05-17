package com.example.premove.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.premove.data.local.entity.WorkflowRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowRunDao {
    @Query("Select * from workflow_runs where id=:workflowRunId")
    suspend fun getworkflowRunById(workflowRunId: String): WorkflowRunEntity

    @Query("Select * from workflow_runs where workflowId=:workflowId order by createdAt desc limit 1")
    suspend fun getLatestWorkflowRunByWorkflowId(workflowId: String): WorkflowRunEntity

    @Query("Select * from workflow_runs where workflowId=:workflowId order by createdAt desc")
    fun getWorkflowRunsByWorkflowId(workflowId: String): Flow<List<WorkflowRunEntity>>

    @Insert
    suspend fun insertworkflowRun(workflowRun: WorkflowRunEntity)

    @Update
    suspend fun updateworkflowRun(workflowRun: WorkflowRunEntity)

    @Query("Delete from workflow_runs where id = :workflowRunId")
    suspend fun deleteworkflowRunById(workflowRunId: String)
}