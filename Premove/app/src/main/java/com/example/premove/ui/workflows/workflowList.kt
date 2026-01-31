package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.premove.model.WorkflowEntity

@Composable
fun WorkflowList(
    filteredWorkflows: List<WorkflowEntity>,
    toggleWorkflow: (String) -> Unit,
    onDeleteClicked: (String) -> Unit,
    onWorkflowClick: (String) -> Unit
){
    LazyColumn {
        itemsIndexed(
            items = filteredWorkflows,
            key = { index, workflow -> workflow.id } // IMPORTANT
        ) { index, workflow ->
            Column() {
                Spacer(Modifier.height(10.dp))
                WorkflowCard(
                    workflow,
                    onTap = onWorkflowClick,
                    toggleWorkflow = { toggleWorkflow(workflow.id) },
                    onDeleteWorkflow = {
                        onDeleteClicked(workflow.id)
                    })
                if (index < filteredWorkflows.lastIndex)
                    Divider(
                        color = Color.Gray.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
            }
        }
    }
}