package com.example.premove.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.premove.ui.workflows.AddWorkflowDialog
import com.example.premove.ui.workflows.DeleteDialog
import com.example.premove.ui.workflows.WorkflowList
import com.example.premove.ui.workflows.WorkflowTopBar
import com.example.premove.viewModel.WorkflowViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun Home(
    onWorkflowClick: (String) -> Unit
){
    val workflowViewModel: WorkflowViewModel =  hiltViewModel()
    val filteredWorkflows by workflowViewModel.filteredWorkflows.collectAsState()
    val selectedDeleteWorkflowId: String? by workflowViewModel.selectedDeleteWorkflowId.collectAsState(initial = null)
    val selectedDeleteWorkflow = selectedDeleteWorkflowId?.let { id -> filteredWorkflows.find { it.id == id } }
    val searchQuery by workflowViewModel.searchQuery.collectAsState()
    var isAddWorkflowDialogOpen by remember { mutableStateOf(false) }

    Column() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        WorkflowTopBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = {
                                workflowViewModel.onSearchQueryChange(it)
                            }
                        )
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
                        isAddWorkflowDialogOpen = true
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                WorkflowList(
                    filteredWorkflows = filteredWorkflows,
                    toggleWorkflow = workflowViewModel::toggleWorkflow,
                    onDeleteClicked = workflowViewModel::onDeleteClicked,
                    onWorkflowClick = onWorkflowClick
                )
            }
        }
    }

    // todo :  this needs to move to workflow page
    selectedDeleteWorkflow?.let { workflow ->
        DeleteDialog(
            selectedWorkflow = workflow,
            onConfirmDelete = {
                workflowViewModel.deleteWorkflow(workflow.id)
                workflowViewModel.onDeleteDialogDismissed()
            },
            onDismiss = workflowViewModel::onDeleteDialogDismissed
        )
    }

    if (isAddWorkflowDialogOpen) {
        AddWorkflowDialog(
            onCreate = workflowViewModel::addWorkflow,
            onDismiss = {
                isAddWorkflowDialogOpen = false
            }
        )
    }
}