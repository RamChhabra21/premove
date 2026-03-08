package com.example.premove.ui.workflows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.premove.data.local.entity.WorkflowRunEntity
import com.example.premove.viewModel.WorkflowUpdateParams
import com.example.premove.viewModel.WorkflowViewModel
import java.text.SimpleDateFormat
import java.util.*

// =============================================================================
// TriggerType — maps to/from WorkflowEntity.triggerType string column
// =============================================================================

enum class TriggerType(val label: String, val icon: ImageVector, val key: String) {
    MANUAL("Manual",       Icons.Outlined.TouchApp, "MANUAL"),
    SCHEDULED("Scheduled", Icons.Outlined.Schedule, "SCHEDULED"),
    WEBHOOK("Webhook",     Icons.Outlined.Webhook,  "WEBHOOK"),
    EVENT("Event",         Icons.Outlined.Bolt,     "EVENT")
}

private fun String.toTriggerType(): TriggerType =
    TriggerType.entries.firstOrNull { it.key == this } ?: TriggerType.MANUAL

// =============================================================================
// Run status helpers
// =============================================================================

private fun runStatusColor(status: String): Color = when (status.lowercase()) {
    "completed" -> Color(0xFF22C55E)
    "failed"    -> Color(0xFFEF4444)
    "running"   -> Color(0xFF3B82F6)
    else        -> Color(0xFF94A3B8)
}

private fun runStatusIcon(status: String): ImageVector = when (status.lowercase()) {
    "completed" -> Icons.Outlined.CheckCircle
    "failed"    -> Icons.Outlined.Cancel
    "running"   -> Icons.Outlined.Autorenew
    else        -> Icons.Outlined.Circle
}

// =============================================================================
// Screen
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowConfig(
    workflowId: String,
    workflowViewModel: WorkflowViewModel,
    onBack: () -> Unit = {},
    onDelete: () -> Unit = {},
    recentRuns: List<WorkflowRunEntity> = emptyList(),
    nodeCount: Int = 0
) {
    var workflow by remember { mutableStateOf<com.example.premove.data.local.entity.WorkflowEntity?>(null) }
    LaunchedEffect(workflowId) { workflow = workflowViewModel.getWorkflowById(workflowId) }

    val current = workflow ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // ── Editable state — all initialised from the loaded WorkflowEntity ───────
    var title          by remember(current.id) { mutableStateOf(current.title) }
    var description    by remember(current.id) { mutableStateOf(current.description) }
    var isEnabled      by remember(current.id) { mutableStateOf(current.isEnabled) }
    var triggerType    by remember(current.id) { mutableStateOf(current.triggerType.toTriggerType()) }
    var cronExpression by remember(current.id) { mutableStateOf(current.cronExpression) }
    var webhookSecret  by remember(current.id) { mutableStateOf(current.webhookSecret) }
    var timeoutMinutes by remember(current.id) { mutableIntStateOf(current.timeoutMinutes) }
    var maxRetries     by remember(current.id) { mutableIntStateOf(current.maxRetries) }

    // Save button only lights up when something actually changed
    val isDirty by remember {
        derivedStateOf {
            title.trim()       != current.title          ||
                    description.trim() != current.description    ||
                    isEnabled          != current.isEnabled      ||
                    triggerType.key    != current.triggerType    ||
                    cronExpression     != current.cronExpression ||
                    webhookSecret      != current.webhookSecret  ||
                    timeoutMinutes     != current.timeoutMinutes ||
                    maxRetries         != current.maxRetries
        }
    }

    val snackbarHost     = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var visible          by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    // ── Delete dialog ─────────────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title  = { Text("Delete \"${current.title}\"?", fontWeight = FontWeight.SemiBold) },
            text   = { Text("This will permanently remove the workflow and all its nodes, edges, and run history.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        workflowViewModel.deleteWorkflow(workflowId)
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor   = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Delete", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Workflow Settings",
                                style      = MaterialTheme.typography.titleMedium,
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Text(
                                "ID: $workflowId",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        EnabledChip(enabled = isEnabled, onClick = { isEnabled = !isEnabled })
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            // Saves ALL fields — every param maps to a real DB column
                            workflowViewModel.updateWorkflowByObj(
                                workflowId,
                                WorkflowUpdateParams(
                                    title          = title.trim(),
                                    description    = description.trim(),
                                    isEnabled      = isEnabled,
                                    triggerType    = triggerType.key,
                                    cronExpression = cronExpression,
                                    webhookSecret  = webhookSecret,
                                    timeoutMinutes = timeoutMinutes,
                                    maxRetries     = maxRetries,
                                    updatedAt      = System.currentTimeMillis()
                                )
                            )
                        },
                        enabled = isDirty && title.isNotBlank(),
                        colors  = ButtonDefaults.textButtonColors(
                            contentColor         = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.35f)
                        )
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor     = Color.White
                )
            )
        }
    ) { padding ->

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn() + slideInVertically { it / 12 }
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── 1. General ─────────────────────────────────────────────
                ConfigSection("General", Icons.Outlined.Info) {
                    OutlinedTextField(
                        value          = title,
                        onValueChange  = { title = it },
                        label          = { Text("Workflow Name") },
                        leadingIcon    = { Icon(Icons.Outlined.DriveFileRenameOutline, null) },
                        modifier       = Modifier.fillMaxWidth(),
                        singleLine     = true,
                        isError        = title.isBlank(),
                        supportingText = if (title.isBlank()) {{ Text("Name cannot be empty") }} else null,
                        shape          = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = description,
                        onValueChange = { description = it },
                        label         = { Text("Description") },
                        leadingIcon   = { Icon(Icons.Outlined.Notes, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        maxLines      = 5,
                        shape         = RoundedCornerShape(12.dp)
                    )
                }

                // ── 2. Trigger ─────────────────────────────────────────────
                ConfigSection("Trigger", Icons.Outlined.Bolt) {
                    Text(
                        "How should this workflow start?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    TriggerSelector(selected = triggerType, onSelect = { triggerType = it })

                    AnimatedVisibility(
                        visible = triggerType == TriggerType.SCHEDULED,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value          = cronExpression,
                                onValueChange  = { cronExpression = it },
                                label          = { Text("Cron Expression") },
                                leadingIcon    = { Icon(Icons.Outlined.Schedule, null) },
                                supportingText = { Text("e.g.  0 9 * * 1-5  → weekdays at 9 AM") },
                                modifier       = Modifier.fillMaxWidth(),
                                singleLine     = true,
                                shape          = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = triggerType == TriggerType.WEBHOOK,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value          = webhookSecret,
                                onValueChange  = { webhookSecret = it },
                                label          = { Text("Webhook Secret") },
                                leadingIcon    = { Icon(Icons.Outlined.Key, null) },
                                supportingText = { Text("Leave blank to auto-generate on save") },
                                modifier       = Modifier.fillMaxWidth(),
                                singleLine     = true,
                                shape          = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // ── 3. Execution ───────────────────────────────────────────
                ConfigSection("Execution", Icons.Outlined.PlayCircle) {
                    Column {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Timer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Engine Timeout", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            ConfigBadge("$timeoutMinutes min")
                        }
                        Slider(
                            value         = timeoutMinutes.toFloat(),
                            onValueChange = { timeoutMinutes = it.toInt() },
                            valueRange    = 1f..30f,
                            steps         = 28,
                            modifier      = Modifier.padding(horizontal = 2.dp),
                            colors        = SliderDefaults.colors(
                                thumbColor       = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            "Max time the engine allows this workflow to run per cycle",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(Modifier.padding(vertical = 10.dp))

                    Column {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Replay, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Max Retries", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            ConfigBadge(if (maxRetries == 0) "Off" else "${maxRetries}×")
                        }
                        Slider(
                            value         = maxRetries.toFloat(),
                            onValueChange = { maxRetries = it.toInt() },
                            valueRange    = 0f..10f,
                            steps         = 9,
                            modifier      = Modifier.padding(horizontal = 2.dp),
                            colors        = SliderDefaults.colors(
                                thumbColor       = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            "Retry failed nodes before marking the run as failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── 4. Details (read-only) ─────────────────────────────────
                ConfigSection("Details", Icons.Outlined.Summarize) {
                    MetaRow(Icons.Outlined.Tag,           "Workflow ID",   current.id)
                    MetaDivider()
                    MetaRow(Icons.Outlined.Person,        "Created by",    "User #${current.createdBy}")
                    MetaDivider()
                    MetaRow(Icons.Outlined.CalendarToday, "Created",       current.createdAt.toDateString())
                    MetaDivider()
                    MetaRow(Icons.Outlined.Update,        "Last updated",  current.updatedAt.toDateString())
                    if (nodeCount > 0) {
                        MetaDivider()
                        MetaRow(Icons.Outlined.AccountTree, "Nodes", "$nodeCount node${if (nodeCount != 1) "s" else ""}")
                    }
                }

                // ── 5. Run History ─────────────────────────────────────────
                ConfigSection("Run History", Icons.Outlined.History) {
                    if (recentRuns.isEmpty()) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.HourglassEmpty, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("No runs yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        recentRuns.forEachIndexed { index, run ->
                            RunRow(run)
                            if (index < recentRuns.lastIndex) MetaDivider()
                        }
                    }
                }

                // ── 6. Danger Zone ─────────────────────────────────────────
                ConfigSection(
                    title          = "Danger Zone",
                    icon           = Icons.Outlined.WarningAmber,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                    titleColor     = MaterialTheme.colorScheme.error
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Delete workflow", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
                            Text("Permanently removes this workflow and all its data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border  = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MaterialTheme.colorScheme.error)),
                            shape   = RoundedCornerShape(10.dp)
                        ) { Text("Delete", fontWeight = FontWeight.SemiBold) }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// =============================================================================
// Sub-components
// =============================================================================

@Composable
private fun EnabledChip(enabled: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (enabled) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
        label = "chipBg"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(
            if (enabled) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.35f)
        ))
        Spacer(Modifier.width(5.dp))
        Text(
            if (enabled) "Active" else "Paused",
            color      = Color.White,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TriggerSelector(selected: TriggerType, onSelect: (TriggerType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TriggerType.entries.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { type ->
                    val isSelected = type == selected
                    val borderColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "border"
                    )
                    val bgColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), label = "bg"
                    )
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { onSelect(type) },
                        color = bgColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(type.icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(type.label, style = MaterialTheme.typography.bodySmall, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RunRow(run: WorkflowRunEntity) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(runStatusIcon(run.status), null, tint = runStatusColor(run.status), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(run.status.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = runStatusColor(run.status))
            Text(run.createdAt.toDateString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("#${run.id.takeLast(6)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ConfigBadge(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ConfigSection(
    title: String,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    titleColor: Color     = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = containerColor, tonalElevation = 1.dp, shadowElevation = 1.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = titleColor)
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MetaDivider() = HorizontalDivider(Modifier.padding(vertical = 7.dp))

private fun Long.toDateString(): String =
    SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(this))