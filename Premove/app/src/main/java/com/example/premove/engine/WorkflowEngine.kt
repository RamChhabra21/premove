package com.example.premove.engine

import android.util.Log
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.data.local.entity.WorkflowRunEntity
import com.example.premove.data.repository.EdgeRepository
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.example.premove.data.repository.EdgeRunRepository
import com.example.premove.domain.model.NodeCategory
import com.example.premove.domain.model.NodeRegistry
import com.example.premove.network.LlmClient
import com.example.premove.ui.nodes.NodeStatus
import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes


class WorkflowEngine @Inject constructor(
    private val workflowRepository: WorkflowRepository,
    private val workflowRunRepository: WorkflowRunRepository,
    private val nodeRepository: NodeRepository,
    private val nodeRunRepository: NodeRunRepository,
    private val edgeRepository: EdgeRepository,
    private val edgeRunRepository: EdgeRunRepository,
    private val llmClient: LlmClient,
    private val jobTracker: JobTracker,
    private val nodeExecutor: NodeExecutor,
    private val scope: CoroutineScope
) {
    suspend fun execute() {
        val activeWorkflows = workflowRepository.getActiveWorkflows()
        if (activeWorkflows.isEmpty()) {
            // Log.v("WorkflowEngine", "No active workflows to execute")
            return
        }

        activeWorkflows.forEach { workflow ->
            scope.launch {
                try {
                    withTimeout(5.minutes) {
                        executeWorkflow(workflow.id)
                    }
                } catch (e: Exception) {
                    Log.e("WorkflowEngine", "Error executing workflow ${workflow.id}", e)
                }
            }
        }
    }

    suspend fun executeWorkflow(workflowId: String){
        val latestWorkflowRun = workflowRunRepository.getLatestWorkflowRunByWorkflowId(workflowId)
        if (latestWorkflowRun == null) {
            Log.w("WorkflowEngine", "No run found for workflow $workflowId")
            return
        }

        if (latestWorkflowRun.status == "Completed" || latestWorkflowRun.status == "Failed") {
            return
        }

        val runId = latestWorkflowRun.id

        // phase 1 : initiate ready nodes
        val readyNodeRuns = nodeRunRepository.getNodeRunsByWorkflowRunIdandStatus(runId, NodeStatus.READY)
        if (readyNodeRuns.isNotEmpty()) {
            Log.d("WorkflowEngine", "🚀 Phase 1: Found ${readyNodeRuns.size} READY nodes for run $runId")
            coroutineScope {
                readyNodeRuns.map { nodeRun ->
                    async {
                        val updated = nodeRunRepository.compareandUpdateNodeRunStatus(nodeRun.nodeId, runId,
                            NodeStatus.READY, NodeStatus.RUNNING)
                        if (updated > 0) {
                            executeNode(nodeRun)
                        }
                    }
                }.awaitAll()
            }
        }

        // phase 2 : move processed to completed
        val processedNodeRuns = nodeRunRepository.getNodeRunsByWorkflowRunIdandStatus(runId, NodeStatus.PROCESSED)
        if (processedNodeRuns.isNotEmpty()) {
            Log.d("WorkflowEngine", "🏁 Phase 2: Found ${processedNodeRuns.size} PROCESSED nodes for run $runId")
            coroutineScope {
                processedNodeRuns.map { nodeRun ->
                    async {
                        val updated = nodeRunRepository.compareandUpdateNodeRunStatus(nodeRun.nodeId, runId,
                            NodeStatus.PROCESSED, NodeStatus.COMPLETED)
                        if (updated > 0) {
                            Log.d("WorkflowEngine", "➡️ Progressing from completed node ${nodeRun.nodeId}")
                            val outgoingEdges = edgeRepository.getEdgesBySourceNodeId(nodeRun.nodeId, workflowId)
                            
                            Log.d("WorkflowEngine", "📡 Evaluating ${outgoingEdges.size} outgoing edges for node ${nodeRun.nodeId}")
                            val edgesWithDataToFire = llmClient.evaluateEdges(nodeRun.outputData, outgoingEdges)
                            Log.d("WorkflowEngine", "🔥 LLM decided to fire ${edgesWithDataToFire.size} edges")

                            edgesWithDataToFire.map { edgeResult ->
                                async {
                                    val edge = outgoingEdges.find { it.id == edgeResult.edgeId }
                                    if (edge != null) {
                                        val targetNode = nodeRepository.getNodeById(edge.targetNodeId.toInt())
                                        val isTrigger = targetNode?.let {
                                            NodeRegistry.getCategory(it.type) == NodeCategory.TRIGGER
                                        } ?: false

                                        Log.d("WorkflowEngine", "🔗 Marking target node ${edge.targetNodeId} as potential READY (isTrigger=$isTrigger)")
                                        nodeRunRepository.incrementAndMarkReadyIfAvailable(
                                            edge.targetNodeId.toInt(),
                                            runId,
                                            edgeResult.inputData,
                                            isTrigger = isTrigger
                                        )
                                    }
                                }
                            }.awaitAll()
                        }
                    }
                }.awaitAll()
            }
        }

        // phase 3: check RUNNING nodes with jobId (async nodes)
        val asyncNodeRuns = nodeRunRepository.getRunningNodesWithJobId(runId)
        if (asyncNodeRuns.isNotEmpty()) {
            coroutineScope {
                asyncNodeRuns.map { nodeRun ->
                    async {
                        val jobId = nodeRun.jobId ?: return@async
                        when (val data = jobTracker.getJobData(jobId)) {
                            is JobResult.Done -> {
                                Log.i("WorkflowEngine", "✅ Async job $jobId completed for node ${nodeRun.nodeId}")
                                nodeRunRepository.updateNodeOutputData(nodeRun.nodeId, runId, data.output)
                                nodeRunRepository.compareandUpdateNodeRunStatus(
                                    nodeRun.nodeId, runId,
                                    NodeStatus.RUNNING, NodeStatus.PROCESSED
                                )
                            }
                            is JobResult.Failed -> {
                                Log.e("WorkflowEngine", "❌ Async job $jobId failed for node ${nodeRun.nodeId}")
                                nodeRunRepository.compareandUpdateNodeRunStatus(
                                    nodeRun.nodeId, runId,
                                    NodeStatus.RUNNING, NodeStatus.FAILED
                                )
                            }
                            is JobResult.InProgress -> { /* next cycle checks again */ }
                        }
                    }
                }.awaitAll()
            }
        }

        // Check for workflow completion
        checkAndHandleWorkflowCompletion(workflowId, runId)
    }

    private suspend fun checkAndHandleWorkflowCompletion(workflowId: String, runId: String) {
        val allNodes = nodeRepository.getNodesByWorkflowId(workflowId)
        val nodeRuns = nodeRunRepository.getNodeRunsByWorkflowRunId(runId)

        if (allNodes.isEmpty()) return

        val allCompleted = allNodes.all { node ->
            nodeRuns.any { it.nodeId == node.id && it.status == NodeStatus.COMPLETED }
        }

        if (allCompleted) {
            Log.d("WorkflowEngine", "✅ Workflow $workflowId run $runId completed successfully!")
            workflowRunRepository.updateWorkflowRunStatus(runId, "Completed")

            val workflow = workflowRepository.getWorkflowById(workflowId)
            if (workflow.autoReset && workflow.isEnabled) {
                Log.d("WorkflowEngine", "🔄 Auto-reset enabled for $workflowId. Initializing new run.")
                createNewRun(workflowId)
            }
        } else {
            val anyFailed = nodeRuns.any { it.status == NodeStatus.FAILED }
            if (anyFailed) {
                Log.d("WorkflowEngine", "❌ Workflow $workflowId run $runId failed.")
                workflowRunRepository.updateWorkflowRunStatus(runId, "Failed")
            }
        }
    }

    private suspend fun createNewRun(workflowId: String) {
        val workflowRunId = UuidCreator.getTimeOrdered().toString()
        workflowRunRepository.insertWorkflowRun(workflowRunId, workflowId)
        
        val nodes = nodeRepository.getNodesByWorkflowId(workflowId)
        val toInitialiseNodes = nodeRepository.getNodesToBeInitialised(workflowId)
        val initialnodeMap = toInitialiseNodes.associateBy { it.id }

        for (node in nodes) {
            val isInitial = initialnodeMap.containsKey(node.id)
            val isTrigger = NodeRegistry.getCategory(node.type) == NodeCategory.TRIGGER
            val initialStatus = if (isInitial && !isTrigger) NodeStatus.READY else NodeStatus.PENDING

            nodeRunRepository.insertNodeRun(
                NodeRunEntity(
                    id = UuidCreator.getTimeOrdered().toString(),
                    workflowRunId = workflowRunId,
                    nodeId = node.id,
                    status = initialStatus,
                    inputCount = 0
                )
            )
        }
    }

    private suspend fun executeNode(nodeRun: NodeRunEntity) {
        Log.d("WorkflowEngine", "⚙️ Executing node: ${nodeRun.nodeId} (Type: ${nodeRepository.getNodeById(nodeRun.nodeId)?.type})")

        val node = nodeRepository.getNodeById(nodeRun.nodeId) ?: return

        try {
            when (val result = nodeExecutor.execute(node, nodeRun)) {
                is NodeExecutionResult.Completed -> {
                    Log.d("WorkflowEngine", "✅ Node ${node.id} completed successfully")
                    nodeRunRepository.updateNodeOutputData(node.id, nodeRun.workflowRunId, result.output)
                    nodeRunRepository.compareandUpdateNodeRunStatus(
                        node.id,
                        nodeRun.workflowRunId,
                        NodeStatus.RUNNING,
                        NodeStatus.PROCESSED
                    )
                }
                is NodeExecutionResult.Pending -> {
                    Log.d("WorkflowEngine", "⏳ Node ${node.id} is pending with jobId: ${result.jobId}")
                    nodeRunRepository.updateNodeJobId(node.id, nodeRun.workflowRunId, result.jobId)
                }
                is NodeExecutionResult.Failed -> {
                    Log.e("WorkflowEngine", "❌ Node ${node.id} failed: ${result.error}")
                    nodeRunRepository.compareandUpdateNodeRunStatus(
                        node.id,
                        nodeRun.workflowRunId,
                        NodeStatus.RUNNING,
                        NodeStatus.FAILED
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("WorkflowEngine", "💥 Exception during node ${node.id} execution", e)
            nodeRunRepository.compareandUpdateNodeRunStatus(
                node.id,
                nodeRun.workflowRunId,
                NodeStatus.RUNNING,
                NodeStatus.FAILED
            )
        }
    }
}
