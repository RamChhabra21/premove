package com.example.premove.data.repository

import com.example.premove.data.local.dao.EdgeDao
import com.example.premove.data.local.entity.EdgeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EdgeRepository @Inject constructor(
    private val dao: EdgeDao
) {
    suspend fun insertEdge(edge: EdgeEntity) {
        dao.insertEdge(edge)
    }

    suspend fun getEdgesByWorkflowId(workflowId: String) :  Flow<List<EdgeEntity>> {
        return dao.getEdgesByWorkflowId(workflowId)
    }

    suspend fun getEdgesBySourceNodeId(nodeId: Int, workflowId: String): List<EdgeEntity> {
        return dao.getEdgesBySourceNodeId(nodeId, workflowId)
    }

    suspend fun updateEdge(edge: EdgeEntity) {
        dao.updateEdge(edge)
    }
}