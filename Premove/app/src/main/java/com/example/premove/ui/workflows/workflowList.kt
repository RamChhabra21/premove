package com.example.premove.ui.workflows

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.premove.model.WorkflowEntity

@Composable
fun WorkflowList(
    filteredWorkflows: List<WorkflowEntity>,
    toggleWorkflow: (WorkflowEntity) -> Unit,
    onDeleteClicked: (Int) -> Unit
){
    LazyColumn {
        items(
            items = filteredWorkflows,
            key = { it.id } // IMPORTANT
        ) { workflow ->
            WorkflowCard(workflow, toggleWorkflow = {toggleWorkflow(workflow)}, onDeleteWorkflow = {
                onDeleteClicked(workflow.id)
            })
        }
    }
}