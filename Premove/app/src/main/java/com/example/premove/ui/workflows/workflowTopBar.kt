package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkflowTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddWorkflow: () -> Unit
){
    Row() {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                onSearchQueryChange(it)
            },
            placeholder = { Text("search workflow") },
            singleLine = true,
            modifier = Modifier.weight(4f)
        )
        Spacer(
            modifier = Modifier.width(10.dp)
        )
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = {
                onAddWorkflow()
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }

}