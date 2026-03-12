package com.example.premove.ui.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.premove.domain.model.NodeLayoutType
import com.example.premove.ui.workflows.PortType

enum class NodeStatus {
    PENDING, RUNNING, COMPLETED, FAILED, READY, PROCESSED
}

private fun nodeTypeAccentColor(type: String): Color = when (type.uppercase()) {
    "WEB_AGENT"  -> Color(0xFF2196F3)
    "ALARM"      -> Color(0xFF9C27B0)
    "LOCATION"   -> Color(0xFFFF5722)
    "EMAIL"      -> Color(0xFF00897B)
    "DATABASE"   -> Color(0xFF5C6BC0)
    "WEBHOOK"    -> Color(0xFFE91E63)
    else         -> Color(0xFF607D8B)
}

private fun nodeTypeIcon(type: String): String = when (type.uppercase()) {
    "WEB_AGENT"  -> "🌐"
    "ALARM"      -> "🔔"
    "LOCATION"   -> "📍"
    "EMAIL"      -> "✉️"
    "DATABASE"   -> "🗄️"
    "WEBHOOK"    -> "⚡"
    else         -> "⚙️"
}

private fun statusBarColor(status: NodeStatus, accentColor: Color): Color = when (status) {
    NodeStatus.COMPLETED -> Color(0xFF4CAF50)
    NodeStatus.FAILED    -> Color(0xFFF44336)
    NodeStatus.RUNNING   -> Color(0xFFFBC02D)
    NodeStatus.READY     -> Color(0xFF29B6F6)
    NodeStatus.PROCESSED -> Color(0xFF26A69A)
    else                 -> accentColor
}

private fun statusBorderColor(status: NodeStatus, isSelected: Boolean): Color = when (status) {
    NodeStatus.PENDING   -> if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
    NodeStatus.RUNNING   -> Color(0xFFFBC02D)
    NodeStatus.COMPLETED -> Color(0xFF4CAF50)
    NodeStatus.FAILED    -> Color(0xFFF44336)
    NodeStatus.READY     -> Color(0xFF29B6F6)
    NodeStatus.PROCESSED -> Color(0xFF26A69A)
}

private fun statusBackgroundColor(status: NodeStatus): Color = when (status) {
    NodeStatus.COMPLETED -> Color(0xFFF6FBF6)
    NodeStatus.FAILED    -> Color(0xFFFFF8F8)
    NodeStatus.RUNNING   -> Color(0xFFFFFDF5)
    NodeStatus.READY     -> Color(0xFFF5FBFF)
    NodeStatus.PROCESSED -> Color(0xFFF4FAFA)
    else                 -> Color.White
}

private fun statusBadgeColor(status: NodeStatus): Color = when (status) {
    NodeStatus.RUNNING   -> Color(0xFFFBC02D)
    NodeStatus.COMPLETED -> Color(0xFF4CAF50)
    NodeStatus.FAILED    -> Color(0xFFF44336)
    NodeStatus.READY     -> Color(0xFF29B6F6)
    NodeStatus.PROCESSED -> Color(0xFF26A69A)
    else                 -> Color.Transparent
}

private fun statusBadgeLabel(status: NodeStatus): String = when (status) {
    NodeStatus.RUNNING   -> "…"
    NodeStatus.COMPLETED -> "✓"
    NodeStatus.FAILED    -> "✕"
    NodeStatus.READY     -> "●"
    NodeStatus.PROCESSED -> "✓"
    else                 -> ""
}

@Composable
fun Node(
    id: Int,
    position: Offset,
    size: Dp = 120.dp,
    title: String = "Node",
    type: String = "Action",
    layoutType: NodeLayoutType = NodeLayoutType.HORIZONTAL,
    isSelected: Boolean = false,
    status: NodeStatus = NodeStatus.PENDING,
    onClick: () -> Unit = {},
    onPositionChange: (Offset) -> Unit = {},
    onLayoutTypeChange: (NodeLayoutType) -> Unit = {},
    onEdgeDragStart: (sourceNodeId: Int, startPosition: Offset, portType: PortType) -> Unit = { _, _, _ -> },
    onEdgeDrag: (currentPosition: Offset) -> Unit = {},
    onEdgeDragEnd: (targetNodeId: Int?, endPosition: Offset) -> Unit = { _, _ -> },
    isValidInputDropTarget: Boolean = false,
    isValidOutputDropTarget: Boolean = false
) {
    var currentPosition by remember(id) { mutableStateOf(position) }
    var currentLayout by remember(id) { mutableStateOf(layoutType) }

    LaunchedEffect(position) { currentPosition = position }
    LaunchedEffect(layoutType) { currentLayout = layoutType }

    val accentColor = nodeTypeAccentColor(type)
    val borderColor = statusBorderColor(status, isSelected)
    val nodeHeight = size * 0.6f   // 72.dp — matches WorkflowRenderer
    val portSize = 14.dp

    Box(
        modifier = Modifier
            .offset { IntOffset(currentPosition.x.toInt(), currentPosition.y.toInt()) }
    ) {
        // Selection ring
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(width = size + 8.dp, height = nodeHeight + 8.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF2196F3).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        // Main card — drag only
        Box(
            modifier = Modifier
                .size(width = size, height = nodeHeight)
                .shadow(
                    elevation = if (isSelected) 6.dp else 2.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = when {
                        isValidInputDropTarget || isValidOutputDropTarget -> Color(0xFFE8F5E9)
                        else -> statusBackgroundColor(status)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = when {
                        status == NodeStatus.RUNNING -> 1.5.dp
                        isSelected -> 1.5.dp
                        isValidInputDropTarget || isValidOutputDropTarget -> 1.5.dp
                        else -> 0.5.dp
                    },
                    color = when {
                        isValidInputDropTarget || isValidOutputDropTarget -> Color(0xFF4CAF50)
                        else -> borderColor
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .pointerInput(id) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            currentPosition += dragAmount
                            onPositionChange(currentPosition)
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top accent / status bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(statusBarColor(status, accentColor))
                )

                // Content row — tap only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(nodeHeight - 3.dp)
                        .padding(horizontal = 8.dp)
                        .pointerInput(id) {
                            detectTapGestures(onTap = { onClick() })
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Small icon circle
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(accentColor.copy(alpha = 0.1f), CircleShape)
                            .border(0.5.dp, accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nodeTypeIcon(type),
                            fontSize = 9.sp,
                            lineHeight = 9.sp
                        )
                    }

                    // Title + type
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = type.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            fontSize = 9.sp,
                            color = Color(0xFF9E9E9E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 11.sp
                        )
                    }

                    // Status badge
                    if (status != NodeStatus.PENDING) {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .background(statusBadgeColor(status), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = statusBadgeLabel(status),
                                fontSize = 7.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 7.sp
                            )
                        }
                    }
                }
            }
        }

        // ── PORTS ──

        when (currentLayout) {
            NodeLayoutType.HORIZONTAL -> {
                // INPUT port (left)
                Box(
                    modifier = Modifier
                        .offset(x = -(portSize / 2), y = nodeHeight / 2 - portSize / 2)
                        .size(portSize)
                        .pointerInput("input_$id") {
                            var dragPosition = Offset.Zero
                            detectDragGestures(
                                onDragStart = {
                                    dragPosition = Offset(
                                        currentPosition.x,
                                        currentPosition.y + (nodeHeight / 2).toPx()
                                    )
                                    onEdgeDragStart(id, dragPosition, PortType.INPUT)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    onEdgeDrag(dragPosition)
                                },
                                onDragEnd = { onEdgeDragEnd(null, Offset.Zero) }
                            )
                        }
                        .background(
                            if (isValidInputDropTarget) Color(0xFF4CAF50) else Color.White,
                            CircleShape
                        )
                        .border(
                            width = if (isValidInputDropTarget) 2.dp else 1.5.dp,
                            color = if (isValidInputDropTarget) Color(0xFF1B5E20) else Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                )

                // OUTPUT port (right)
                Box(
                    modifier = Modifier
                        .offset(x = size - portSize / 2, y = nodeHeight / 2 - portSize / 2)
                        .size(portSize)
                        .pointerInput("output_$id") {
                            var dragPosition = Offset.Zero
                            detectDragGestures(
                                onDragStart = {
                                    dragPosition = Offset(
                                        currentPosition.x + size.toPx(),
                                        currentPosition.y + (nodeHeight / 2).toPx()
                                    )
                                    onEdgeDragStart(id, dragPosition, PortType.OUTPUT)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    onEdgeDrag(dragPosition)
                                },
                                onDragEnd = { onEdgeDragEnd(null, Offset.Zero) }
                            )
                        }
                        .background(
                            if (isValidOutputDropTarget) Color(0xFF2196F3) else Color.White,
                            CircleShape
                        )
                        .border(
                            width = if (isValidOutputDropTarget) 2.dp else 1.5.dp,
                            color = if (isValidOutputDropTarget) Color(0xFF0D47A1) else Color(0xFF2196F3),
                            shape = CircleShape
                        )
                )
            }

            NodeLayoutType.VERTICAL -> {
                // INPUT port (top)
                Box(
                    modifier = Modifier
                        .offset(x = size / 2 - portSize / 2, y = -(portSize / 2))
                        .size(portSize)
                        .pointerInput("input_$id") {
                            var dragPosition = Offset.Zero
                            detectDragGestures(
                                onDragStart = {
                                    dragPosition = Offset(
                                        currentPosition.x + (size / 2).toPx(),
                                        currentPosition.y
                                    )
                                    onEdgeDragStart(id, dragPosition, PortType.INPUT)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    onEdgeDrag(dragPosition)
                                },
                                onDragEnd = { onEdgeDragEnd(null, Offset.Zero) }
                            )
                        }
                        .background(
                            if (isValidInputDropTarget) Color(0xFF4CAF50) else Color.White,
                            CircleShape
                        )
                        .border(
                            width = if (isValidInputDropTarget) 2.dp else 1.5.dp,
                            color = if (isValidInputDropTarget) Color(0xFF1B5E20) else Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                )

                // OUTPUT port (bottom)
                Box(
                    modifier = Modifier
                        .offset(x = size / 2 - portSize / 2, y = nodeHeight - portSize / 2)
                        .size(portSize)
                        .pointerInput("output_$id") {
                            var dragPosition = Offset.Zero
                            detectDragGestures(
                                onDragStart = {
                                    dragPosition = Offset(
                                        currentPosition.x + (size / 2).toPx(),
                                        currentPosition.y + nodeHeight.toPx()
                                    )
                                    onEdgeDragStart(id, dragPosition, PortType.OUTPUT)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragPosition += dragAmount
                                    onEdgeDrag(dragPosition)
                                },
                                onDragEnd = { onEdgeDragEnd(null, Offset.Zero) }
                            )
                        }
                        .background(
                            if (isValidOutputDropTarget) Color(0xFF2196F3) else Color.White,
                            CircleShape
                        )
                        .border(
                            width = if (isValidOutputDropTarget) 2.dp else 1.5.dp,
                            color = if (isValidOutputDropTarget) Color(0xFF0D47A1) else Color(0xFF2196F3),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}