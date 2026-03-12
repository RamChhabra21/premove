package com.example.premove.engine

import android.util.Log
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.data.repository.EdgeRepository
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.example.premove.data.repository.EdgeRunRepository
import com.example.premove.network.LlmClient
import com.example.premove.ui.nodes.NodeStatus
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
    private val llmClient: LlmClient,   // add this
    private val scope: CoroutineScope
) {
    suspend fun execute() {
        // find active workflows first
        val activeWorkflows = workflowRepository.getActiveWorkflows()

        // for each active workflow (and refer to workflow config if we need to run a workflow or not)
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

        // phase 1 : initiate ready nodes
        val readyNodeRuns = nodeRunRepository.getNodeRunsByWorkflowRunIdandStatus(latestWorkflowRun?.id ?: "",
            NodeStatus.READY)


        coroutineScope {
            readyNodeRuns.map { nodeRun ->
                async {
                    // mark this node as running
                    val updated = nodeRunRepository.compareandUpdateNodeRunStatus(nodeRun.nodeId, latestWorkflowRun?.id ?: "",
                        NodeStatus.READY,NodeStatus.RUNNING)
                    if(updated > 0){
                        // execute node
                        executeNode(nodeRun)
                    }
                }
            }.awaitAll()
        }

        // phase 2 : move processed to completed
        val processedNodeRuns = nodeRunRepository.getNodeRunsByWorkflowRunIdandStatus(latestWorkflowRun?.id ?: "",
            NodeStatus.PROCESSED)

        coroutineScope {
            processedNodeRuns.map { nodeRun ->
                async {
                    // mark this node as COMPLETED
                    val updated = nodeRunRepository.compareandUpdateNodeRunStatus(nodeRun.nodeId, latestWorkflowRun?.id ?: "",
                        NodeStatus.PROCESSED,NodeStatus.COMPLETED)
                    if(updated > 0){
                        val outgoingEdges = edgeRepository.getEdgesBySourceNodeId(nodeRun.nodeId, workflowId)

                        val edgesWithDataToFire = llmClient.evaluateEdges(nodeRun.outputData, outgoingEdges)

                        edgesWithDataToFire.map { edgeResult ->
                            async {
                                val edge = outgoingEdges.first { it.id == edgeResult.edgeId }
                                nodeRunRepository.incrementAndMarkReadyIfAvailable(
                                    edge.targetNodeId.toInt(),
                                    latestWorkflowRun?.id ?: "",
                                    edgeResult.inputData
                                )
                            }
                        }.awaitAll()


//                        // now get next nodes connected to this node and progress this
//                        val nextNodes = nodeRepository.getNextConnectedNodes(nodeRun.nodeId)
//                        // move the flow to next nodes
//                        nextNodes.map { nextNode ->
//                            async {
//                                // increment input reference counter
//                                nodeRunRepository.incrementAndMarkReadyIfAvailable(nextNode.id, latestWorkflowRun?.id ?: "")
//                            }
//                        }.awaitAll()
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun executeNode(nodeRun: NodeRunEntity) {
        // actual node execution logic
        print("executing node : ${nodeRun.nodeId}")

        // get node details here

        var node = nodeRepository.getNodeById(nodeRun.nodeId)

        var outputData=node?.configJson!!

        // update output data for the node Run after execution completion
        nodeRunRepository.updateNodeOutputData(nodeRun.nodeId, nodeRun.workflowRunId, outputData)

        // mark node run as processed
        nodeRunRepository.compareandUpdateNodeRunStatus(
            nodeRun.nodeId,
            nodeRun.workflowRunId,
            NodeStatus.RUNNING,
            NodeStatus.PROCESSED
        )
    }
}