package com.example.premove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.premove.ui.theme.PreMoverTheme
import com.example.premove.ui.workflows.WorkflowTopBar
import com.example.premove.ui.workflows.DeleteDialog
import com.example.premove.ui.workflows.WorkflowList
import com.example.premove.viewModel.WorkflowViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreMoverTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize().offset(x=0.dp,y=50.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    MyComp()
                }
            }
        }
    }
}

@Composable
fun MyComp(){
    val workflowViewModel: WorkflowViewModel = androidx.lifecycle.viewmodel.compose.viewModel();
    val filteredWorkflows by workflowViewModel.filteredWorkflows.collectAsState()
    val selectedDeleteWorkflowId: Int? by workflowViewModel.selectedDeleteWorkflowId.collectAsState(initial = null)
    val selectedDeleteWorkflow = selectedDeleteWorkflowId?.let { id -> filteredWorkflows.find { it.id == id } }
    val searchQuery by workflowViewModel.searchQuery.collectAsState()

    Column() {
        WorkflowTopBar(
            searchQuery = searchQuery,
            onSearchQueryChange = {
                workflowViewModel.onSearchQueryChange(it)
            },
            onAddWorkflow = {
                workflowViewModel.addWorkflow("New workflow", "", true, 1)
            }
        )

        WorkflowList(
            filteredWorkflows = filteredWorkflows,
            toggleWorkflow = workflowViewModel::toggleWorkflow,
            onDeleteClicked = workflowViewModel::onDeleteClicked
        )
    }

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
}