package com.example.premove.ui.workflows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.premove.ui.utility.WorkflowSearchBar

@Composable
fun WorkflowTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
){
    var isSearchBarOpen by remember { mutableStateOf(false) }
    Row() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Premove")
            if(!isSearchBarOpen) {
                IconButton(
                    onClick = {
                        isSearchBarOpen = true
                    }
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Add")
                }
            }
            else{
                WorkflowSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onClose = {
                        isSearchBarOpen = false
                    }
                )
            }
        }
    }
}
