package com.example.premove.engine

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.example.premove.ui.nodes.NodeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes


class WorkflowEngine(
    private val workflowRepository: WorkflowRepository,
    private val workflowRunRepository: WorkflowRunRepository,
    private val nodeRepository: NodeRepository,
    private val nodeRunRepository: NodeRunRepository,
    private val scope: CoroutineScope
) {
    suspend fun execute() {
        // find active workflows first
        val activeWorkflows = workflowRepository.getActiveWorkflows()

        // for each active workflow
        activeWorkflows.forEach { workflow ->
            scope.launch {
                withTimeout(5.minutes) {
                    executeWorkflow(workflow.id)
                }
            }
        }
    }

    suspend fun executeWorkflow(workflowId: String){
        // get latest workflowRunId
        val latestWorkflowRun =  workflowRunRepository.getLatestWorkflowRunByWorkflowId(workflowId)
        // get all ready nodes
        val readyNodes = nodeRunRepository.getNodeRunsByWorkflowRunIdandStatus(latestWorkflowRun?.id ?: "",
            NodeStatus.READY)
        // now get next nodes connected to this node and progress this
    }

    private fun executeNode(node: NodeEntity) {
        // actual node execution logic

    }
}