package com.example.premove.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.ui.nodes.NodeStatus
import com.example.premove.viewModel.NodeWithStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeRunDao {
    @Query("Select * from node_runs where workflowRunId=:workflowRunId")
    suspend fun getNodeRunsByWorkflowRunId(workflowRunId: String): List<NodeRunEntity>

    @Query("Select * from node_runs where workflowRunId=:workflowRunId and status=:status")
    suspend fun getNodeRunsByWorkflowRunIdandStatus(workflowRunId: String, status: NodeStatus): List<NodeRunEntity>

    @Query("select * from node_runs where workflowrunid = (select id from workflow_runs where workflowid=:workflowId order by createdat desc limit 1)")
    fun getLatestNodeRunByWorkflowId(workflowId: String): Flow<List<NodeRunEntity>>

    @Query("Select * from node_runs where id=:nodeRunId")
    suspend fun getNodeRunById(nodeRunId: String): NodeRunEntity

    @Insert
    suspend fun insertNodeRun(nodeRun: NodeRunEntity)

    @Update
    suspend fun updateNodeRun(nodeRun: NodeRunEntity)

    @Query("Delete from node_runs where id = :nodeRunId")
    suspend fun deleteNodeRunById(nodeRunId: String)
}