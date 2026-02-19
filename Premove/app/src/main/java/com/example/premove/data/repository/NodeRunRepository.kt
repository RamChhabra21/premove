package com.example.premove.data.repository

import com.example.premove.data.local.dao.NodeRunDao
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.ui.nodes.NodeStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NodeRunRepository @Inject constructor(
    private val dao: NodeRunDao
) {
    suspend fun getNodeRunsByWorkflowRunId(workflowRunId: String): List<NodeRunEntity>{
        return dao.getNodeRunsByWorkflowRunId(workflowRunId)
    }

    suspend fun getNodeRunByNodeIdandWorkflowRunId(nodeId: Int, workflowRunId: String): NodeRunEntity{
        return dao.getNodeRunByNodeIdandWorkflowRunId(nodeId, workflowRunId)
    }

    suspend fun getNodeRunsByWorkflowRunIdandStatus(workflowRunId: String, status: NodeStatus): List<NodeRunEntity>{
        return dao.getNodeRunsByWorkflowRunIdandStatus(workflowRunId, status)
    }

    fun getLatestNodeRunByWorkflowId(workflowId: String): Flow<List<NodeRunEntity>>{
        return dao.getLatestNodeRunByWorkflowId(workflowId)
    }

    suspend fun insertNodeRun(nodeRunEntity: NodeRunEntity){
        dao.insertNodeRun(nodeRunEntity)
    }

    suspend fun updateNodeRunStatus(nodeId: Int, workflowRunId: String, status: NodeStatus): Int{
        return dao.updateNodeRunStatus(nodeId, workflowRunId, status)
    }

    suspend fun compareandUpdateNodeRunStatus(nodeId: Int, workflowRunId: String, expectedStatus: NodeStatus, newStatus: NodeStatus): Int{
        return dao.compareAndUpdateNodeRunStatus(nodeId, workflowRunId, expectedStatus, newStatus)
    }

    suspend fun incrementAndMarkReadyIfAvailable(nodeId: Int, workflowRunId: String): Int {
        return dao.incrementAndMarkReadyIfAvailable(nodeId, workflowRunId)
    }
}