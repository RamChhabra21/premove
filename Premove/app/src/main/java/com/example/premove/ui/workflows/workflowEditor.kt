package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.premove.data.local.entity.WorkflowEntity
import com.example.premove.ui.utility.InteractiveDottedCanvas
import com.example.premove.viewModel.WorkflowEditorViewModel
import com.example.premove.viewModel.WorkflowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditor(
    workflowId: String,
    workflowViewModel: WorkflowViewModel,
    workflowEditorViewModel: WorkflowEditorViewModel,
    onNodeClick: (Int) -> Unit,
    onDelete: () -> Unit = {}
) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    workflowEditorViewModel.setWorkflowId(workflowId)
    var workflow by remember { mutableStateOf<WorkflowEntity?>(null) }
    val selectedDeleteWorkflowId: String? by workflowViewModel.selectedDeleteWorkflowId.collectAsState(initial = null)

    LaunchedEffect (workflowId) {
        workflow = workflowViewModel.getWorkflowById(workflowId)
        workflowEditorViewModel.setWorkflowId(workflowId)
    }

    // Wait for workflow to load
    val currentWorkflow = workflow ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // any composable goes here
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Workflow Editor")
                        IconButton(
                            onClick = {
                                workflowViewModel.onDeleteClicked(workflowId)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                        },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    // here add a new node
                    workflowEditorViewModel.createNode(
                        title = "New Node",
                        type = "web",
                        position = offset,
                        configJson = ""
                    )
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->  // <- get Scaffold padding here
        androidx.compose.material3.Surface(
            modifier = Modifier
                .padding(paddingValues) // respect top bar
                .padding(10.dp),       // additional padding
        ) {
            InteractiveDottedCanvas(workflowId, onNodeClick)
        }
        selectedDeleteWorkflowId?.let { workflowId ->
            DeleteDialog(
                selectedWorkflow = currentWorkflow,
                onConfirmDelete = {
                    workflowViewModel.deleteWorkflow(workflowId)
                    workflowViewModel.onDeleteDialogDismissed()
                    onDelete()
                },
                onDismiss = workflowViewModel::onDeleteDialogDismissed
            )
        }
    }
}
