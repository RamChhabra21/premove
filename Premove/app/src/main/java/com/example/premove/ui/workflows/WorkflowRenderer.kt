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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.premove.data.local.entity.EdgeEntity
import com.example.premove.domain.model.NodeLayoutType
import com.example.premove.ui.nodes.Node
import com.example.premove.viewModel.WorkflowEditorViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

enum class PortType {
    INPUT, OUTPUT
}

data class EdgeDragState(
    val sourceNodeId: Int,
    val startPosition: Offset,
    val currentPosition: Offset,
    val portType: PortType  // Which port is being dragged
)

data class NodeData(
    val id: Int,
    val position: Offset,
    val title: String,
    val type: String,
    val layoutType: NodeLayoutType
)

data class EdgeData(
    val id: Int
)

@Composable
fun WorkflowRenderer(workflowId: String) {
    val workflowEditorViewModel: WorkflowEditorViewModel = hiltViewModel()

    LaunchedEffect(workflowId) {
        workflowEditorViewModel.setWorkflowId(workflowId)
    }

    val dbNodes by workflowEditorViewModel.nodes.collectAsState()
    var localNodes by workflowEditorViewModel.localNodes
    var localEdges by workflowEditorViewModel.localEdges
    val dbEdges by workflowEditorViewModel.edges.collectAsState()

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

    LaunchedEffect(dbEdges) {
        localEdges = dbEdges.map { edge ->
            EdgeEntity(
                id = edge.id,
                workflowId = edge.workflowId,
                sourceNodeId = edge.sourceNodeId,
                targetNodeId = edge.targetNodeId
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw existing edges and temporary edge
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw existing edges
            localEdges.forEach { edge: EdgeEntity ->
                val sourceNode = localNodes.find { it.id.toString() == edge.sourceNodeId }
                val targetNode = localNodes.find { it.id.toString() == edge.targetNodeId }

                if (sourceNode != null && targetNode != null) {
                    val startPos = getOutputPortPosition(sourceNode)
                    val endPos = getInputPortPosition(targetNode)
                    drawDirectedEdge(startPos, endPos)
                }
            }

            // Draw temporary edge while dragging
            edgeDragState?.let { state ->
                drawDirectedEdge(
                    state.startPosition,
                    state.currentPosition,
                    isDashed = true
                )
            }
        }

        // Render all nodes
        localNodes.forEach { node ->
            val isValidDropTarget = edgeDragState?.let { state ->
                if (state.sourceNodeId == node.id) {
                    false // Can't connect to self
                } else if (state.portType == PortType.OUTPUT) {
                    // Dragging from output, can drop on input
                    isNearPort(state.currentPosition, getInputPortPosition(node))
                } else {
                    // Dragging from input, can drop on output
                    isNearPort(state.currentPosition, getOutputPortPosition(node))
                }
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
                onEdgeDragStart = { sourceNodeId, startPos, portType ->
                    edgeDragState = EdgeDragState(
                        sourceNodeId = sourceNodeId,
                        startPosition = startPos,
                        currentPosition = startPos,
                        portType = portType
                    )
                },
                onEdgeDrag = { currentPos ->
                    edgeDragState = edgeDragState?.copy(currentPosition = currentPos)
                },
                onEdgeDragEnd = { _, _ ->
                    edgeDragState?.let { state ->
                        // Find target node based on which port type we're dragging from
                        val targetNode = localNodes.find { targetNode ->
                            if (targetNode.id == state.sourceNodeId) {
                                false  // Can't connect to self
                            } else if (state.portType == PortType.OUTPUT) {
                                // We're dragging FROM output, so check if we're near ANY input port
                                isNearPort(state.currentPosition, getInputPortPosition(targetNode))
                            } else {
                                // We're dragging FROM input, so check if we're near ANY output port
                                isNearPort(state.currentPosition, getOutputPortPosition(targetNode))
                            }
                        }

                        println("target node :  ${targetNode?.id}")

                        if (targetNode != null) {
                            // Determine correct source/target based on which port we dragged from
                            val (sourceId, targetId) = if (state.portType == PortType.OUTPUT) {
                                // Dragged from output → source is drag node, target is drop node
                                state.sourceNodeId.toString() to targetNode.id.toString()
                            } else {
                                // Dragged from input → source is drop node, target is drag node
                                targetNode.id.toString() to state.sourceNodeId.toString()
                            }

                            println("edge is here")

                            workflowEditorViewModel.createEdge(
                                EdgeEntity(
                                    id = UUID.randomUUID().toString(),
                                    workflowId = workflowId,
                                    sourceNodeId = sourceId,
                                    targetNodeId = targetId
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
            node.position.x + 120,
            node.position.y + 36
        )
        NodeLayoutType.VERTICAL -> Offset(
            node.position.x + 60,
            node.position.y + 72
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

private fun isNearPort(dragPos: Offset, portPos: Offset, threshold: Float = 30f): Boolean {
    val distance = sqrt(
        (dragPos.x - portPos.x).pow(2) + (dragPos.y - portPos.y).pow(2)
    )
    return distance < threshold
}

// Bold black directed edge with arrow
private fun DrawScope.drawDirectedEdge(
    start: Offset,
    end: Offset,
    isDashed: Boolean = false
) {
    val arrowSize = 12f
    val arrowOffset = 8f
    val strokeWidth = 4f
    val color = Color.Black

    // Calculate direction for arrow
    val dx = end.x - start.x
    val dy = end.y - start.y
    val distance = sqrt(dx * dx + dy * dy)

    // Adjust end point to leave space for arrow
    val adjustedEnd = Offset(
        end.x - (dx / distance) * arrowOffset,
        end.y - (dy / distance) * arrowOffset
    )

    // Draw bezier curve
    val offset = (distance * 0.4f).coerceIn(40f, 200f)

    val controlPoint1 = Offset(start.x + offset, start.y)
    val controlPoint2 = Offset(adjustedEnd.x - offset, adjustedEnd.y)

    val path = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            adjustedEnd.x, adjustedEnd.y
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth)
    )

    // Draw arrow (only if not dashed)
    if (!isDashed) {
        val angle = atan2(dy, dx) * 180f / Math.PI.toFloat()

        rotate(angle, end) {
            val arrowPath = Path().apply {
                moveTo(end.x, end.y)
                lineTo(end.x - arrowSize, end.y - arrowSize * 0.5f)
                lineTo(end.x - arrowSize, end.y + arrowSize * 0.5f)
                close()
            }
            drawPath(arrowPath, color)
        }
    }
}