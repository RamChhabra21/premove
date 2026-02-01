package com.example.premove.ui.workflows

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.premove.ui.utility.InteractiveDottedCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowEditor(workflowId: String) {
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
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
            InteractiveDottedCanvas(workflowId)
        }
    }
}
