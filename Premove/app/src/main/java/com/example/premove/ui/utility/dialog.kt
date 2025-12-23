package com.example.premove.ui.utility

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

@Composable
fun DialogBox(
    text: String,
    onclickfunc: () -> Unit,
    handleCloseDialog: () -> Unit
){
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.50f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                handleCloseDialog()
            },
        contentAlignment = Alignment.Center
    ){
        Box(
            Modifier.size(200.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.padding(bottom=5.dp),
                    text = "Confirm Deletion"
                )
                Text(text)
                Button(
                    onClick = {
                        onclickfunc()
                    },
                    modifier=Modifier.padding(top=5.dp)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}