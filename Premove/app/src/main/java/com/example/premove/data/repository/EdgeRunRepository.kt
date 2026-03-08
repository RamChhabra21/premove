package com.example.premove.data.repository

import com.example.premove.data.local.dao.EdgeRunDao
import com.example.premove.data.local.entity.EdgeRunEntity
import javax.inject.Inject

class EdgeRunRepository @Inject constructor(
    private val dao: EdgeRunDao
) {
    suspend fun insert(edgeRun: EdgeRunEntity) = dao.insert(edgeRun)

    suspend fun getByWorkflowRunId(workflowRunId: String): List<EdgeRunEntity> =
        dao.getByWorkflowRunId(workflowRunId)
}