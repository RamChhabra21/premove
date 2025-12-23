package com.example.premove

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.premove.ui.theme.PreMoverTheme
import com.example.premove.ui.workflows.WorkflowTopBar
import com.example.premove.ui.workflows.DeleteDialog
import com.example.premove.ui.workflows.WorkflowList
import com.example.premove.viewModel.WorkflowViewModel
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.premove.ui.workflows.AddWorkflowDialog


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreMoverTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize().offset(x=0.dp,y=0.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Home()
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun Home(){
    val workflowViewModel: WorkflowViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val filteredWorkflows by workflowViewModel.filteredWorkflows.collectAsState()
    val selectedDeleteWorkflowId: Int? by workflowViewModel.selectedDeleteWorkflowId.collectAsState(initial = null)
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
                    onDeleteClicked = workflowViewModel::onDeleteClicked
                )
            }
        }
    }

    // todo :  this needs to move to workflow page
    selectedDeleteWorkflow?.let {
        workflow ->
        DeleteDialog(
            selectedWorkflow = workflow,
            handleDeleteWorkFlow = {
                workflowViewModel.deleteWorkflow(workflow)
                workflowViewModel.onDeleteDialogDismissed()
            },
            handleCloseDialog = workflowViewModel::onDeleteDialogDismissed
        )
    }

    if(isAddWorkflowDialogOpen) {
        AddWorkflowDialog(
            onCreate = workflowViewModel::addWorkflow,
            onClose = {
                isAddWorkflowDialogOpen = false
            },
            onSkip = {
                isAddWorkflowDialogOpen = false
            }
        )
    }
}