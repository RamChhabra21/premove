package com.example.premove.data.repository

import androidx.lifecycle.viewModelScope
import com.example.premove.data.local.dao.WorkflowRunDao
import com.example.premove.data.local.entity.WorkflowEntity
import com.example.premove.data.local.entity.WorkflowRunEntity
import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.coroutines.launch
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
}