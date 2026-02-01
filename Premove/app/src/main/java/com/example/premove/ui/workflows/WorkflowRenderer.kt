package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.premove.ui.nodes.Node
import com.example.premove.viewModel.WorkflowEditorViewModel
import com.example.premove.viewModel.WorkflowViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun WorkflowRenderer(workflowId: String) {
    val workflowViewModel: WorkflowViewModel = hiltViewModel()
    val workflowEditorViewModel: WorkflowEditorViewModel = hiltViewModel()

    // Local state for nodes (you can move this to ViewModel later)
    var nodes by remember {
        mutableStateOf(
            listOf(
                NodeData(
                    id = "node1",
                    position = Offset(100f, 100f),
                    title = "Trigger",
                    type = "Webhook"
                ),
                NodeData(
                    id = "node2",
                    position = Offset(300f, 150f),
                    title = "Send Email",
                    type = "Action"
                )
            )
        )
    }

    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    // Render all nodes
    nodes.forEach { node ->
        Node(
            id = node.id,
            position = node.position,
            title = node.title,
            type = node.type,
            isSelected = selectedNodeId == node.id,
            onClick = {
                selectedNodeId = node.id
            },
            onPositionChange = { newPosition ->
                // Update node position in state
                nodes = nodes.map {
                    if (it.id == node.id) it.copy(position = newPosition)
                    else it
                }
            }
        )
    }
}

// Data class for node data
data class NodeData(
    val id: String,
    val position: Offset,
    val title: String,
    val type: String
)