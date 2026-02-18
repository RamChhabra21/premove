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

    suspend fun getNodeRunsByWorkflowRunIdandStatus(workflowRunId: String, status: NodeStatus): List<NodeRunEntity>{
        return dao.getNodeRunsByWorkflowRunIdandStatus(workflowRunId, status)
    }

    fun getLatestNodeRunByWorkflowId(workflowId: String): Flow<List<NodeRunEntity>>{
        return dao.getLatestNodeRunByWorkflowId(workflowId)
    }

    suspend fun insertNodeRun(nodeRunEntity: NodeRunEntity){
        dao.insertNodeRun(nodeRunEntity)
    }
}