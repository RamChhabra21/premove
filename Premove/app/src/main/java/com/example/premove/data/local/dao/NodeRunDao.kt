package com.example.premove.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("Select * from node_runs where nodeId=:nodeId and workflowRunId=:workflowRunId")
    suspend fun getNodeRunByNodeIdandWorkflowRunId(nodeId: Int, workflowRunId: String): NodeRunEntity

    @Query("Select * from node_runs where id=:nodeRunId")
    suspend fun getNodeRunById(nodeRunId: String): NodeRunEntity

    @Query("Select * from node_runs where status='RUNNING' and workflowRunId=:workflowRunId is not null")
    suspend fun getRunningNodesWithJobId(workflowRunId: String): List<NodeRunEntity>

    @Insert
    suspend fun insertNodeRun(nodeRun: NodeRunEntity)

    @Query("update node_runs set status=:status where nodeId=:nodeId and workflowRunId=:workflowRunId")
    suspend fun updateNodeRunStatus(nodeId: Int, workflowRunId: String, status: NodeStatus): Int

    @Query("update node_runs set outputData=:outputData where nodeId=:nodeId and workflowRunId=:workflowRunId")
    suspend fun updateNodeOutputData(nodeId: Int, workflowRunId: String, outputData: String): Int

    @Query("UPDATE node_runs SET status=:newStatus WHERE nodeId=:nodeId AND workflowRunId=:workflowRunId AND status=:expectedStatus")
    suspend fun compareAndUpdateNodeRunStatus(nodeId: Int, workflowRunId: String, expectedStatus: NodeStatus, newStatus: NodeStatus): Int

    @Query("""
    UPDATE node_runs 
    SET inputCount = inputCount + 1,
        inputData = :inputData,
        status = CASE 
            WHEN inputCount + 1 = (SELECT COUNT(*) FROM edges WHERE targetNodeId = :nodeId AND workflowId = (SELECT workflowId FROM workflow_runs WHERE id = :workflowRunId))
            THEN 'READY' 
            ELSE status 
        END
    WHERE nodeId = :nodeId AND workflowRunId = :workflowRunId
""")
    suspend fun incrementAndMarkReadyIfAvailable(nodeId: Int, workflowRunId: String, inputData: String?): Int

    @Query("Update node_runs set jobId=:jobId where nodeId=:nodeId and workflowRunId=:workflowRunId")
    suspend fun updateNodeJobId(nodeId: Int, workflowRunId: String, jobId: String): Int

    @Update
    suspend fun updateNodeRun(nodeRun: NodeRunEntity)

    @Query("Delete from node_runs where id = :nodeRunId")
    suspend fun deleteNodeRunById(nodeRunId: String)
}