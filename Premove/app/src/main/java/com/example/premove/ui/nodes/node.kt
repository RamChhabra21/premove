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

@Composable
fun Node(
    id: String,
    position: Offset,
    size: Dp = 120.dp,
    title: String = "Node",
    type: String = "Action",
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onPositionChange: (Offset) -> Unit = {}
) {
    var currentPosition by remember { mutableStateOf(position) }

    // Sync with external position changes
    LaunchedEffect(position) {
        currentPosition = position
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(currentPosition.x.toInt(), currentPosition.y.toInt()) }
            .size(width = size, height = size * 0.6f)
            .pointerInput(id) {
                detectDragGestures(
                    onDragStart = {
                        onClick() // Select node when drag starts
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // dragAmount is in world space thanks to graphicsLayer
                        currentPosition += dragAmount
                    },
                    onDragEnd = {
                        // Notify parent of final position
                        onPositionChange(currentPosition)
                    }
                )
            }
            .shadow(
                elevation = if (isSelected) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when (type) {
                                "Trigger" -> Color.Green
                                "Action" -> Color.Blue
                                "Condition" -> Color.Yellow
                                else -> Color.Gray
                            },
                            shape = CircleShape
                        )
                )
            }

            Text(
                text = type,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Connection points
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-6).dp)
                .size(12.dp)
                .background(Color.Gray, CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 6.dp)
                .size(12.dp)
                .background(Color.Gray, CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}