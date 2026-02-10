package com.example.premove.ui.workflows

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
    val density = LocalDensity.current

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
                targetNodeId = edge.targetNodeId,
                bendX = edge.bendX,
                bendY = edge.bendY
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw existing edges and temporary edge
        Canvas(modifier = Modifier.fillMaxSize()) {
            localEdges.forEach { edge: EdgeEntity ->
                val sourceNode = localNodes.find { it.id.toString() == edge.sourceNodeId }
                val targetNode = localNodes.find { it.id.toString() == edge.targetNodeId }

                if (sourceNode != null && targetNode != null) {
                    val startPos = getOutputPortPosition(sourceNode, density.density)
                    val endPos = getInputPortPosition(targetNode, density.density)
                    drawDirectedEdge(startPos, endPos, edge.bendX, edge.bendY)
                }
            }

            edgeDragState?.let { state ->
                drawDirectedEdge(state.startPosition, state.currentPosition, isDashed = true)
            }
        }

        // Bend handles
        localEdges.forEach { edge ->
            val sourceNode = localNodes.find { it.id.toString() == edge.sourceNodeId }
            val targetNode = localNodes.find { it.id.toString() == edge.targetNodeId }

            if (sourceNode != null && targetNode != null) {
                val startPos = getOutputPortPosition(sourceNode, density.density)
                val endPos = getInputPortPosition(targetNode, density.density)

                val handleX = (startPos.x + endPos.x) / 2 + edge.bendX
                val handleY = (startPos.y + endPos.y) / 2 + edge.bendY

                Box(
                    modifier = Modifier
                        .offset { IntOffset(handleX.toInt(), handleY.toInt()) }
                        .size(9.dp)
                        .pointerInput(edge.id) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    localEdges = localEdges.map {
                                        if (it.id == edge.id) it.copy(
                                            bendX = it.bendX + dragAmount.x,
                                            bendY = it.bendY + dragAmount.y
                                        ) else it
                                    }
                                },
                                onDragEnd = {
                                    val updatedEdge = localEdges.find { it.id == edge.id }
                                    if (updatedEdge != null) {
                                        println("updateEdge : $updatedEdge")
                                        workflowEditorViewModel.updateEdge(
                                            updatedEdge
                                        )
                                    }
                                }
                            )
                        }
                        .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                )
            }
        }

        // Render all nodes
        localNodes.forEach { node ->
            val isValidInputDropTarget = edgeDragState?.let { state ->
                if (state.sourceNodeId == node.id) false
                else if (state.portType == PortType.OUTPUT) {
                    isNearPort(state.currentPosition, getInputPortPosition(node, density.density))
                } else false
            } ?: false

            val isValidOutputDropTarget = edgeDragState?.let { state ->
                if (state.sourceNodeId == node.id) false
                else if (state.portType == PortType.INPUT) {
                    isNearPort(state.currentPosition, getOutputPortPosition(node, density.density))
                } else false
            } ?: false

            Node(
                id = node.id,
                position = node.position,
                title = node.title,
                type = node.type,
                layoutType = node.layoutType,
                isSelected = selectedNodeId == node.id,
                isValidInputDropTarget = isValidInputDropTarget,
                isValidOutputDropTarget = isValidOutputDropTarget,
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
                        val targetNode = localNodes.find { targetNode ->
                            if (targetNode.id == state.sourceNodeId) {
                                false
                            } else if (state.portType == PortType.OUTPUT) {
                                isNearPort(state.currentPosition, getInputPortPosition(targetNode, density.density))
                            } else {
                                isNearPort(state.currentPosition, getOutputPortPosition(targetNode, density.density))
                            }
                        }

                        if (targetNode != null) {
                            val (sourceId, targetId) = if (state.portType == PortType.OUTPUT) {
                                state.sourceNodeId.toString() to targetNode.id.toString()
                            } else {
                                targetNode.id.toString() to state.sourceNodeId.toString()
                            }

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

// Update the helper functions
private fun getOutputPortPosition(node: NodeData, density: Float): Offset {
    val sizePx = 120 * density
    val nodeHeightPx = sizePx * 0.6f
    return when (node.layoutType) {
        NodeLayoutType.HORIZONTAL -> Offset(
            node.position.x + sizePx,
            node.position.y + nodeHeightPx / 2
        )
        NodeLayoutType.VERTICAL -> Offset(
            node.position.x + sizePx / 2,
            node.position.y + nodeHeightPx
        )
    }
}

private fun getInputPortPosition(node: NodeData, density: Float): Offset {
    val sizePx = 120 * density
    val nodeHeightPx = sizePx * 0.6f
    return when (node.layoutType) {
        NodeLayoutType.HORIZONTAL -> Offset(
            node.position.x,
            node.position.y + nodeHeightPx / 2
        )
        NodeLayoutType.VERTICAL -> Offset(
            node.position.x + sizePx / 2,
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

// CHANGE 1: drawDirectedEdge now accepts bendX, bendY
private fun DrawScope.drawDirectedEdge(
    start: Offset,
    end: Offset,
    bendX: Float = 0f,
    bendY: Float = 0f,
    isDashed: Boolean = false
) {
    val arrowSize = 30f
    val arrowOffset = 8f
    val strokeWidth = 4f
    val color = Color.Black

    val dx = end.x - start.x
    val dy = end.y - start.y
    val distance = sqrt(dx * dx + dy * dy)

    val adjustedEnd = Offset(
        end.x - (dx / distance) * arrowOffset,
        end.y - (dy / distance) * arrowOffset
    )

    val offset = (distance * 0.4f).coerceIn(40f, 200f)

    // CHANGE 2: direction-aware control points + bend offset
    val nx = (dx / distance) * offset
    val ny = (dy / distance) * offset
    val controlPoint1 = Offset(start.x + nx + bendX * 0.5f, start.y + ny + bendY * 0.5f)
    val controlPoint2 = Offset(adjustedEnd.x - nx + bendX * 0.5f, adjustedEnd.y - ny + bendY * 0.5f)

    val path = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            adjustedEnd.x, adjustedEnd.y
        )
    }

    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))

    if (!isDashed) {
        val nearEnd = bezierPoint(0.99f, start, controlPoint1, controlPoint2, adjustedEnd)
        val angle = atan2(adjustedEnd.y - nearEnd.y, adjustedEnd.x - nearEnd.x) * 180f / Math.PI.toFloat()

        rotate(angle, pivot = adjustedEnd) {
            val arrowPath = Path().apply {
                moveTo(adjustedEnd.x - arrowSize, adjustedEnd.y - arrowSize * 0.5f)
                lineTo(adjustedEnd.x, adjustedEnd.y)
                lineTo(adjustedEnd.x - arrowSize, adjustedEnd.y + arrowSize * 0.5f)
                close()
            }
            drawPath(arrowPath, color)
        }
    }
}

private fun bezierPoint(t: Float, p0: Offset, p1: Offset, p2: Offset, p3: Offset): Offset {
    val mt = 1 - t
    return Offset(
        mt*mt*mt*p0.x + 3*mt*mt*t*p1.x + 3*mt*t*t*p2.x + t*t*t*p3.x,
        mt*mt*mt*p0.y + 3*mt*mt*t*p1.y + 3*mt*t*t*p2.y + t*t*t*p3.y
    )
}