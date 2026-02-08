package com.example.premove.data.repository

import com.example.premove.data.local.dao.WorkflowDao
import com.example.premove.data.local.entity.WorkflowEntity
import javax.inject.Inject

class WorkflowRepository @Inject constructor(
    private val dao: WorkflowDao
) {

    fun getAllWorkflows() = dao.getAllWorkflows()

    suspend fun getWorkflowById(workflowId: String) {
        dao.getWorkflowById(workflowId)
    }

    suspend fun insertWorkflow(workflow: WorkflowEntity) {
        dao.insertWorkflow(workflow)
    }

    suspend fun updateWorkflow(workflow: WorkflowEntity){
        dao.updateWorkflow(workflow)
    }

    suspend fun deleteWorkflow(workflowId: String){
        dao.deleteWorkflowById(workflowId = workflowId)
    }

    suspend fun toggleWorkflow(workflowId: String) {
        dao.toggleWorkflow(workflowId = workflowId)
    }
}

