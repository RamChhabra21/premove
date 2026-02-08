package com.example.premove.ui.workflows

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.premove.data.local.entity.EdgeEntity
import com.example.premove.domain.model.NodeLayoutType
import com.example.premove.ui.nodes.Node
import com.example.premove.viewModel.WorkflowEditorViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun WorkflowRenderer(workflowId: String) {
    val workflowEditorViewModel: WorkflowEditorViewModel = hiltViewModel()

    LaunchedEffect(workflowId) {
        workflowEditorViewModel.setWorkflowId(workflowId)
    }

    val dbNodes by workflowEditorViewModel.nodes.collectAsState()
    var localNodes by workflowEditorViewModel.localNodes
//    val edges by workflowEditorViewModel.edges.collectAsState()

    // Edge dragging state
    var edgeDragState by remember { mutableStateOf<EdgeDragState?>(null) }
    var selectedNodeId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(dbNodes) {
        localNodes = dbNodes.map { node ->
            NodeData(
                id = node.id,
                position = Offset(node.x, node.y),
                title = node.title,
                type = node.type,
                layoutType = node.layoutType
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw existing edges and temporary edge
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw existing edges
//            edges.forEach { edge: EdgeEntity ->
//                val sourceNode = localNodes.find { it.id.toString() == edge.sourceNodeId }
//                val targetNode = localNodes.find { it.id.toString() == edge.targetNodeId }
//
//                if (sourceNode != null && targetNode != null) {
//                    val startPos = getOutputPortPosition(sourceNode)
//                    val endPos = getInputPortPosition(targetNode)
//                    drawBezierEdge(startPos, endPos, Color(0xFF2196F3))
//                }
//            }

            // Draw temporary edge while dragging
            edgeDragState?.let { state ->
                drawBezierEdge(
                    state.startPosition,
                    state.currentPosition,
                    Color(0xFF64B5F6)
                )
            }
        }

        // Render all nodes
        localNodes.forEach { node ->
            val isValidDropTarget = edgeDragState?.let { state ->
                state.sourceNodeId != node.id &&
                        isNearInputPort(state.currentPosition, getInputPortPosition(node))
            } ?: false

            Node(
                id = node.id,
                position = node.position,
                title = node.title,
                type = node.type,
                layoutType = node.layoutType,
                isSelected = selectedNodeId == node.id,
                isValidDropTarget = isValidDropTarget,
                onClick = {
                    selectedNodeId = node.id
                },
                onPositionChange = { newPosition ->
                    localNodes = localNodes.map {
                        if (it.id == node.id) it.copy(position = newPosition)
                        else it
                    }
                    workflowEditorViewModel.updateNodePositionDebounced(node.id, newPosition)
                },
                onEdgeDragStart = { sourceNodeId, startPos ->
                    edgeDragState = EdgeDragState(
                        sourceNodeId = sourceNodeId,
                        startPosition = startPos,
                        currentPosition = startPos
                    )
                },
                onEdgeDrag = { currentPos ->
                    edgeDragState = edgeDragState?.copy(currentPosition = currentPos)
                },
                onEdgeDragEnd = { _, _ ->
                    edgeDragState?.let { state ->
                        val targetNode = localNodes.find { targetNode ->
                            targetNode.id != state.sourceNodeId &&
                                    isNearInputPort(state.currentPosition, getInputPortPosition(targetNode))
                        }

                        if (targetNode != null) {
                            workflowEditorViewModel.createEdge(
                                EdgeEntity(
                                    id = UUID.randomUUID().toString(),
                                    workflowId = workflowId,
                                    sourceNodeId = state.sourceNodeId.toString(),
                                    targetNodeId = targetNode.id.toString()
                                )
                            )
                        }
                    }
                    edgeDragState = null
                }
            )
        }
    }
}

// Helper functions
private fun getOutputPortPosition(node: NodeData): Offset {
    return when (node.layoutType) {
        NodeLayoutType.HORIZONTAL -> Offset(
            node.position.x + 120, // node width
            node.position.y + 36   // node height / 2
        )
        NodeLayoutType.VERTICAL -> Offset(
            node.position.x + 60,  // node width / 2
            node.position.y + 72   // node height
        )
    }
}

private fun getInputPortPosition(node: NodeData): Offset {
    return when (node.layoutType) {
        NodeLayoutType.HORIZONTAL -> Offset(
            node.position.x,
            node.position.y + 36
        )
        NodeLayoutType.VERTICAL -> Offset(
            node.position.x + 60,
            node.position.y
        )
    }
}

private fun isNearInputPort(dragPos: Offset, portPos: Offset, threshold: Float = 30f): Boolean {
    val distance = sqrt(
        (dragPos.x - portPos.x).pow(2) + (dragPos.y - portPos.y).pow(2)
    )
    return distance < threshold
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBezierEdge(
    start: Offset,
    end: Offset,
    color: Color
) {
    val path = Path().apply {
        moveTo(start.x, start.y)

        val controlPoint1 = Offset(start.x + 100, start.y)
        val controlPoint2 = Offset(end.x - 100, end.y)

        cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            end.x, end.y
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3f)
    )
}

data class EdgeDragState(
    val sourceNodeId: Int,
    val startPosition: Offset,
    val currentPosition: Offset
)

data class NodeData(
    val id: Int,
    val position: Offset,
    val title: String,
    val type: String,
    val layoutType: NodeLayoutType
)