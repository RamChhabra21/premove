package com.example.premove.ui.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.premove.domain.model.NodeLayoutType
import com.example.premove.ui.workflows.PortType

@Composable
fun Node(
    id: Int,
    position: Offset,
    size: Dp = 120.dp,
    title: String = "Node",
    type: String = "Action",
    layoutType: NodeLayoutType = NodeLayoutType.HORIZONTAL,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onPositionChange: (Offset) -> Unit = {},
    onEdgeDragStart: (sourceNodeId: Int, startPosition: Offset, portType: PortType) -> Unit = { _, _, _ -> },
    onEdgeDrag: (currentPosition: Offset) -> Unit = {},
    onEdgeDragEnd: (targetNodeId: Int?, endPosition: Offset) -> Unit = { _, _ -> },
    isValidInputDropTarget: Boolean = false,
    isValidOutputDropTarget: Boolean = false
) {
    var currentPosition by remember(id) { mutableStateOf(position) }

    LaunchedEffect(position) {
        currentPosition = position
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(currentPosition.x.toInt(), currentPosition.y.toInt()) }
    ) {
        // Main node body
        Box(
            modifier = Modifier
                .size(width = size, height = size * 0.6f)
                .shadow(
                    elevation = if (isSelected) 8.dp else 4.dp,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.Blue else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(id) {
                            detectDragGestures(
                                onDragStart = { onClick() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentPosition += dragAmount
                                },
                                onDragEnd = { onPositionChange(currentPosition) }
                            )
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Text(
                    text = type,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Ports
        val nodeHeight = size * 0.6f
        val portSize = 14.dp

        when (layoutType) {
            NodeLayoutType.HORIZONTAL -> {
                // INPUT port (left) - DRAGGABLE
                Box(
                    modifier = Modifier
                        .offset(x = -(portSize / 2), y = nodeHeight / 2 - portSize / 2)
                        .size(portSize)
                        .pointerInput("input_$id") {
                            var dragPosition=Offset.Zero
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
                                onDragEnd = {
                                    onEdgeDragEnd(null, Offset.Zero)
                                }
                            )
                        }
                        .background(
                            color = if (isValidInputDropTarget) Color(0xFF66BB6A) else Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                        .border(
                            width = if (isValidInputDropTarget) 3.dp else 2.dp,
                            color = if (isValidInputDropTarget) Color(0xFF1B5E20) else Color.White,
                            shape = CircleShape
                        )
                )

                // OUTPUT port (right) - DRAGGABLE
                Box(
                    modifier = Modifier
                        .offset(x = size - portSize / 2, y = nodeHeight / 2 - portSize / 2)
                        .size(portSize)
                        .pointerInput("output_$id") {
                            var dragPosition=Offset.Zero
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
                                onDragEnd = {
                                    onEdgeDragEnd(null, Offset.Zero)
                                }
                            )
                        }
                    .background(
                        color = if (isValidOutputDropTarget) Color(0xFF1565C0) else Color(0xFF2196F3),
                        shape = CircleShape
                    )
                    .border(
                        width = if (isValidOutputDropTarget) 3.dp else 2.dp,
                        color = if (isValidOutputDropTarget) Color(0xFF0D47A1) else Color.White,
                        shape = CircleShape
                    )
                )
            }

            NodeLayoutType.VERTICAL -> {
                // INPUT port (top) - DRAGGABLE
                Box(
                    modifier = Modifier
                        .offset(x = size / 2 - portSize / 2, y = -(portSize / 2))
                        .size(portSize)
                        .pointerInput("input_$id") {
                            var dragPosition=Offset.Zero
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
                                onDragEnd = {
                                    onEdgeDragEnd(null, Offset.Zero)
                                }
                            )
                        }
                        .background(
                            color = if (isValidInputDropTarget) Color(0xFF66BB6A) else Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                        .border(
                            width = if (isValidInputDropTarget) 3.dp else 2.dp,
                            color = if (isValidInputDropTarget) Color(0xFF1B5E20) else Color.White,
                            shape = CircleShape
                        )
                )

                // OUTPUT port (bottom) - DRAGGABLE
                Box(
                    modifier = Modifier
                        .offset(x = size / 2 - portSize / 2, y = nodeHeight - portSize / 2)
                        .size(portSize)
                        .pointerInput("output_$id") {
                            var dragPosition=Offset.Zero
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
                                onDragEnd = {
                                    onEdgeDragEnd(null, Offset.Zero)
                                }
                            )
                        }
                    .background(
                        color = if (isValidOutputDropTarget) Color(0xFF1565C0) else Color(0xFF2196F3),
                        shape = CircleShape
                    )
                    .border(
                        width = if (isValidOutputDropTarget) 3.dp else 2.dp,
                        color = if (isValidOutputDropTarget) Color(0xFF0D47A1) else Color.White,
                        shape = CircleShape
                    )
                )
            }
        }
    }
}