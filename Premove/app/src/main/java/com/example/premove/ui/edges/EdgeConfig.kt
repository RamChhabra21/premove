package com.example.premove.ui.edges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.premove.viewModel.WorkflowEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeConfig(
    edgeId: String,
    workflowId: String,
    onBack: () -> Unit,
    viewModel: WorkflowEditorViewModel = hiltViewModel()
) {
    LaunchedEffect(workflowId) {
        viewModel.setWorkflowId(workflowId)
    }

    val edges by viewModel.edges.collectAsState()
    val edge = edges.find { it.id == edgeId }

    var conditionText by remember(edge?.condition) {
        mutableStateOf(edge?.condition ?: "")
    }
    var isConditional by remember(edge?.condition) {
        mutableStateOf(edge?.condition != null)
    }
    val hasCondition = conditionText.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edge Condition", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Leave empty to always proceed. Add a condition to let the LLM decide whether to follow this edge.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConditionChip("Always", !isConditional) {
                    isConditional = false
                    conditionText = ""
                }
                ConditionChip("Conditional", isConditional) {
                    isConditional = true
                }
            }

            Spacer(Modifier.height(20.dp))

            if (isConditional) {
                Text(
                    "Condition expression",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                BasicTextField(
                    value = conditionText,
                    onValueChange = { conditionText = it },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (hasCondition) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(14.dp)
                        .heightIn(min = 100.dp),
                    decorationBox = { inner ->
                        if (conditionText.isEmpty()) {
                            Text(
                                "e.g. price < 50  or  is this urgent?",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        inner()
                    }
                )

                Spacer(Modifier.height(12.dp))

                @OptIn(ExperimentalLayoutApi::class)
                (FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "only if urgent",
                        "only if error",
                        "only if sentiment negative",
                        "only if score is high",
                        "only if no reply in 24h",
                        "pass only summary",
                        "pass only errors",
                        "pass full report"
                    ).forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable { conditionText = suggestion }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                suggestion,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                })
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (edge?.condition != null) {
                    OutlinedButton(
                        onClick = {
                            edge?.let {
                                viewModel.updateEdge(it.copy(condition = null))
                            }
                            onBack()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Remove", fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = {
                        edge?.let {
                            val newCondition = if (!isConditional) null else conditionText.trim().ifBlank { null }
                            viewModel.updateEdge(it.copy(condition = newCondition))
                        }
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save", fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConditionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}