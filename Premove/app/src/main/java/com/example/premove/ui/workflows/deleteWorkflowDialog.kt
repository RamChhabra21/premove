package com.example.premove.ui.workflows

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.premove.model.WorkflowEntity
import com.example.premove.ui.utility.DialogBox

@Composable
fun DeleteDialog(
    selectedWorkflow: WorkflowEntity,
    handleDeleteWorkFlow: () -> Unit,
    handleCloseDialog: () -> Unit
){
    DialogBox(
        text = "Are you sure you want to delete ${selectedWorkflow.title}?",
        onclickfunc = handleDeleteWorkFlow,
        handleCloseDialog = handleCloseDialog
    )
}