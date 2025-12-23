package com.example.premove.ui.workflows
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.premove.model.WorkflowEntity


@Composable
fun WorkflowCard(
    workflow: WorkflowEntity,
    toggleWorkflow: (WorkflowEntity) -> Unit,
    onDeleteWorkflow: () -> Unit,
 ){
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp).clickable(onClick = { /* open workflow */ }),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(){
            Text(text = workflow.title,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(text = workflow.description)
        }

        Row() {
            Switch(
                modifier = Modifier.scale(0.8f),
                checked = checked,
                onCheckedChange = {
                    checked = it;
                    toggleWorkflow(workflow)
                }
            )

            IconButton(
                onClick = {
                    onDeleteWorkflow()
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}