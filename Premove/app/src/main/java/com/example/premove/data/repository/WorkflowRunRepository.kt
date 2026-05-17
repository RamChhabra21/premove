package com.example.premove.data.repository

import com.example.premove.data.local.dao.WorkflowRunDao
import com.example.premove.data.local.entity.WorkflowRunEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkflowRunRepository @Inject constructor(
    private val dao: WorkflowRunDao
) {
    suspend fun insertWorkflowRun(workflowRunId: String, workflowId: String){
        dao.insertworkflowRun(
            WorkflowRunEntity(
                id = workflowRunId,
                workflowId= workflowId,
                status = "Active"
            )
        )
    }

    suspend fun getLatestWorkflowRunByWorkflowId(workflowId: String): WorkflowRunEntity? {
        return dao.getLatestWorkflowRunByWorkflowId(workflowId)
    }

    fun getWorkflowRunsByWorkflowId(workflowId: String): Flow<List<WorkflowRunEntity>> {
        return dao.getWorkflowRunsByWorkflowId(workflowId)
    }

    suspend fun updateWorkflowRunStatus(workflowRunId: String, status: String) {
        val run = dao.getworkflowRunById(workflowRunId)
        dao.updateworkflowRun(run.copy(status = status, updatedAt = System.currentTimeMillis()))
    }
}
