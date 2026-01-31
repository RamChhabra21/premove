package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditor(workflowId: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // any composable goes here
                    Text("Workflow Editor")
                        },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->  // <- get Scaffold padding here
        androidx.compose.material3.Surface(
            modifier = Modifier
                .padding(paddingValues) // respect top bar
                .padding(10.dp),       // additional padding
        ) {
            Text(
                "This is the workflow editor screen $workflowId",
                color = MaterialTheme.colorScheme.onBackground, // safe color
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
