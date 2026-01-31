package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.premove.model.WorkflowEntity
import com.example.premove.ui.utility.BaseDialog

@Composable
fun DeleteDialog(
    selectedWorkflow: WorkflowEntity,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    BaseDialog(onDismiss = onDismiss) {
        Column {
            Text(
                text = "Delete Workflow",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Are you sure you want to delete \"${selectedWorkflow.title}\"?"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        onConfirmDelete()
                        onDismiss()
                    }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
