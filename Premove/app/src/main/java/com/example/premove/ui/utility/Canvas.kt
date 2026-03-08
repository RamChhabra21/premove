package com.example.premove.ui.utility

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.premove.ui.workflows.WorkflowRenderer

@Composable
fun InteractiveDottedCanvas(
    workflowId: String,
    onNodeClick: (Int) -> Unit,
) {

    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(workflowId) {
                detectTransformGestures { centroid, pan, zoomChange, _ ->
                    val newZoom = (zoom * zoomChange).coerceIn(0.3f, 5f)
                    offset = (offset + pan - centroid) * (newZoom/zoom) + centroid
                    zoom = newZoom
                }
            }
            .drawBehind {
                val baseSpacing = 100f
                val baseRadius = 3f

                val spacing = baseSpacing * zoom
                val radius = baseRadius * zoom

                val startX = (offset.x % spacing + spacing) % spacing
                val startY = (offset.y % spacing + spacing) % spacing

                var x = startX
                while (x < size.width) {
                    var y = startY
                    while (y < size.height) {
                        drawCircle(
                            color = Color.Gray.copy(0.8f),
                            radius = radius,
                            center = Offset(x, y)
                        )
                        y += spacing
                    }
                    x += spacing
                }
            }
    ) {
        // Apply pan+zoom only to workflow content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                    scaleX = zoom
                    scaleY = zoom
                    transformOrigin = TransformOrigin(0f, 0f) // Scale from top-left
                }
        ) {
            WorkflowRenderer(
                workflowId,
                onNodeClick,
            )
        }
    }
}
