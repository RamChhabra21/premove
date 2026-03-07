package com.example.premove.ui.home

import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.premove.ui.workflows.AddWorkflowDialog
import com.example.premove.ui.workflows.DeleteDialog
import com.example.premove.ui.workflows.WorkflowList
import com.example.premove.ui.workflows.WorkflowTopBar
import com.example.premove.viewModel.WorkflowViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.f4b6a3.uuid.UuidCreator
import com.llamatik.library.platform.LlamaBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun Home(
    onWorkflowClick: (String) -> Unit
){
    val workflowViewModel: WorkflowViewModel =  hiltViewModel()
    val filteredWorkflows by workflowViewModel.filteredWorkflows.collectAsState()
    val searchQuery by workflowViewModel.searchQuery.collectAsState()
    var isAddWorkflowDialogOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

//    // load model
//    val modelPath = LlamaBridge.getModelPath("Qwen_Qwen3-4B-GGUF_Qwen3-4B-Q4_K_M.gguf")
//
//    LlamaBridge.initGenerateModel(modelPath)
//
//    val output = LlamaBridge.generate(
//        "Explain Kotlin Multiplatform in one sentence."
//    )
//
//    println("llm output : $output")

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
                    onWorkflowClick = onWorkflowClick
                )
            }
        }
    }

    if (isAddWorkflowDialogOpen) {
        AddWorkflowDialog(
            onCreate = { title: String, description: String, isEnabled: Boolean, createdBy: Int ->
                val workflowId = UuidCreator.getTimeOrdered().toString()
                // add a new workflow
                workflowViewModel.addWorkflow(id= workflowId, title, description, isEnabled, createdBy)
                // initialise nodes into workflow
                workflowViewModel.initialiseWorkflow(workflowId)
            },
            onDismiss = {
                isAddWorkflowDialogOpen = false
            }
        )
    }
}