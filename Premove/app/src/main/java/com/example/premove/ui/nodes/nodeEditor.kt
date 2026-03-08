package com.example.premove.ui.nodes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import com.example.premove.viewModel.WorkflowEditorViewModel

// Node Types
enum class NodeType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    ALARM(
        "Alarm",
        Icons.Default.Alarm,
        Color(0xFFF44336),
        "Ring an alarm with custom label"
    ),
    LOCATION(
        "Location Check",
        Icons.Default.LocationOn,
        Color(0xFF4CAF50),
        "Monitor device location"
    ),
    WEB_AGENT(
        "Web Agent",
        Icons.Default.Language,
        Color(0xFF9C27B0),
        "Automate web tasks with AI"
    )
}

// Location input mode
enum class LocationInputMode {
    MAP, COORDINATES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeEditor(
    nodeId: Int, // -1 for new node, positive for existing
    viewModel: WorkflowEditorViewModel,
    onBack: () -> Unit = {}
) {
    var nodeName by remember { mutableStateOf("") }
    var selectedNodeType by remember { mutableStateOf<NodeType?>(null) }
    var showNodeTypeSelector by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }

    // Alarm config
    var alarmLabel by remember { mutableStateOf("") }

    // Location config
    var locationInputMode by remember { mutableStateOf(LocationInputMode.COORDINATES) }
    var targetLatitude by remember { mutableStateOf("") }
    var targetLongitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("100") }
    var locationName by remember { mutableStateOf("") }

    // Web Agent config
    var webPrompt by remember { mutableStateOf("") }

    // Load node data from ViewModel
    LaunchedEffect(nodeId) {
        val node = viewModel.getNodeForEditor(nodeId)
        nodeName = node.title
        selectedNodeType = try {
            NodeType.valueOf(node.type)
        } catch (e: Exception) {
            NodeType.WEB_AGENT // fallback
        }

        // Parse config JSON and populate fields
        node.configJson?.let { json ->
            try {
                when (selectedNodeType) {
                    NodeType.ALARM -> {
                        // Parse: {"alarmLabel":"..."}
                        alarmLabel = json.substringAfter("alarmLabel\":\"").substringBefore("\"")
                    }

                    NodeType.LOCATION -> {
                        // Parse: {"latitude":"...","longitude":"...","radius":"...","locationName":"..."}
                        targetLatitude = json.substringAfter("latitude\":\"").substringBefore("\"")
                        targetLongitude =
                            json.substringAfter("longitude\":\"").substringBefore("\"")
                        radius = json.substringAfter("radius\":\"").substringBefore("\"")
                        locationName =
                            json.substringAfter("locationName\":\"").substringBefore("\"")
                    }

                    NodeType.WEB_AGENT -> {
                        // Parse: {"prompt":"..."}
                        webPrompt = json.substringAfter("prompt\":\"").substringBefore("\"")
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                // Keep default empty values
            }
        }

        isLoaded = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedNodeType == null) "Select Node Type" else "Configure Node",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (selectedNodeType != null && isLoaded) {
                        TextButton(
                            onClick = {
                                val config = when (selectedNodeType) {
                                    NodeType.ALARM -> """{"alarmLabel":"$alarmLabel"}"""
                                    NodeType.LOCATION -> """{"latitude":"$targetLatitude","longitude":"$targetLongitude","radius":"$radius","locationName":"$locationName"}"""
                                    NodeType.WEB_AGENT -> """{"prompt":"$webPrompt"}"""
                                    else -> "{}"
                                }

                                viewModel.saveNode(
                                    nodeId = nodeId,
                                    title = nodeName,
                                    type = selectedNodeType!!.name,
                                    configJson = config
                                )

                                onBack()
                            }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (!isLoaded) {
            // Show loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Node Type Selection
                if (selectedNodeType == null) {
                    item {
                        Text(
                            "Choose Node Type",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(NodeType.values().size) { index ->
                        val type = NodeType.values()[index]
                        NodeTypeCard(
                            nodeType = type,
                            onClick = {
                                selectedNodeType = type
                                showNodeTypeSelector = false
                            }
                        )
                    }
                } else {
                    val currentNodeType = selectedNodeType!!

                    // Node Name
                    item {
                        OutlinedTextField(
                            value = nodeName,
                            onValueChange = { nodeName = it },
                            label = { Text("Node Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                    }

                    // Selected Node Type Display
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNodeTypeSelector = true },
                            colors = CardDefaults.cardColors(
                                containerColor = currentNodeType.color.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(2.dp, currentNodeType.color.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(currentNodeType.color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            currentNodeType.icon,
                                            contentDescription = null,
                                            tint = currentNodeType.color,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            currentNodeType.displayName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            currentNodeType.description,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = currentNodeType.color
                                )
                            }
                        }
                    }

                    // Configuration Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Configuration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Type-specific configuration
                    when (currentNodeType) {
                        NodeType.ALARM -> {
                            item {
                                AlarmConfiguration(
                                    alarmLabel = alarmLabel,
                                    onAlarmLabelChange = { alarmLabel = it }
                                )
                            }
                        }

                        NodeType.LOCATION -> {
                            item {
                                LocationConfiguration(
                                    locationInputMode = locationInputMode,
                                    onLocationInputModeChange = { locationInputMode = it },
                                    locationName = locationName,
                                    onLocationNameChange = { locationName = it },
                                    targetLatitude = targetLatitude,
                                    onTargetLatitudeChange = { targetLatitude = it },
                                    targetLongitude = targetLongitude,
                                    onTargetLongitudeChange = { targetLongitude = it },
                                    radius = radius,
                                    onRadiusChange = { radius = it }
                                )
                            }
                        }

                        NodeType.WEB_AGENT -> {
                            item {
                                WebAgentConfiguration(
                                    webPrompt = webPrompt,
                                    onWebPromptChange = { webPrompt = it }
                                )
                            }
                        }
                    }

                    // Delete Button
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Node", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    icon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = {
                        Text(
                            "Delete Node?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text("This action cannot be undone. The node will be permanently removed from the workflow.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                if (nodeId > 0) {
                                    viewModel.deleteNode(nodeId)
                                }
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Node Type Selector Dialog
            if (showNodeTypeSelector && selectedNodeType != null) {
                AlertDialog(
                    onDismissRequest = { showNodeTypeSelector = false },
                    title = { Text("Change Node Type", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NodeType.values().forEach { type ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedNodeType = type
                                            showNodeTypeSelector = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (type == selectedNodeType)
                                            type.color.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (type == selectedNodeType)
                                        BorderStroke(2.dp, type.color)
                                    else
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            type.icon,
                                            contentDescription = null,
                                            tint = type.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                type.displayName,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                type.description,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNodeTypeSelector = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

    @Composable
    fun NodeTypeCard(
        nodeType: NodeType,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = nodeType.color.copy(alpha = 0.1f)
            ),
            border = BorderStroke(2.dp, nodeType.color.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(nodeType.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            nodeType.icon,
                            contentDescription = null,
                            tint = nodeType.color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column {
                        Text(
                            nodeType.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            nodeType.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = nodeType.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    @Composable
    fun AlarmConfiguration(
        alarmLabel: String,
        onAlarmLabelChange: (String) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = alarmLabel,
                onValueChange = onAlarmLabelChange,
                label = { Text("Alarm Label") },
                placeholder = { Text("e.g., Meeting Reminder") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Label, contentDescription = null)
                },
                supportingText = {
                    Text(
                        "Use {{node_id}} to reference data from previous nodes",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF6F00),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "The alarm will ring when this node executes. All other settings (sound, vibration, etc.) will be handled by the system.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Example card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Examples:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Meeting at 3 PM", fontSize = 11.sp)
                    Text("• {{web_agent.event_title}}", fontSize = 11.sp)
                    Text("• Reminder: {{previous_node.task}}", fontSize = 11.sp)
                }
            }
        }
    }

    @Composable
    fun LocationConfiguration(
        locationInputMode: LocationInputMode,
        onLocationInputModeChange: (LocationInputMode) -> Unit,
        locationName: String,
        onLocationNameChange: (String) -> Unit,
        targetLatitude: String,
        onTargetLatitudeChange: (String) -> Unit,
        targetLongitude: String,
        onTargetLongitudeChange: (String) -> Unit,
        radius: String,
        onRadiusChange: (String) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Location Name
            OutlinedTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                label = { Text("Location Name (Optional)") },
                placeholder = { Text("e.g., Home, Office, Gym") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Place, contentDescription = null)
                }
            )

            // Input Mode Selector
            Text(
                "Select Location",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = locationInputMode == LocationInputMode.MAP,
                    onClick = { onLocationInputModeChange(LocationInputMode.MAP) },
                    label = { Text("Pick on Map") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NodeType.LOCATION.color,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = locationInputMode == LocationInputMode.COORDINATES,
                    onClick = { onLocationInputModeChange(LocationInputMode.COORDINATES) },
                    label = { Text("Coordinates") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NodeType.LOCATION.color,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Map or Coordinates
            AnimatedContent(
                targetState = locationInputMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "location_mode"
            ) { mode ->
                when (mode) {
                    LocationInputMode.MAP -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Google Maps iframe or WebView
                                    GoogleMapsView(
                                        latitude = targetLatitude.toDoubleOrNull() ?: 37.7749,
                                        longitude = targetLongitude.toDoubleOrNull() ?: -122.4194,
                                        onLocationSelected = { lat, lng ->
                                            onTargetLatitudeChange(lat.toString())
                                            onTargetLongitudeChange(lng.toString())
                                        }
                                    )
                                }
                            }

                            if (targetLatitude.isNotEmpty() && targetLongitude.isNotEmpty()) {
                                Text(
                                    "Selected: $targetLatitude, $targetLongitude",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    LocationInputMode.COORDINATES -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = targetLatitude,
                                    onValueChange = onTargetLatitudeChange,
                                    label = { Text("Latitude") },
                                    placeholder = { Text("37.7749") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = targetLongitude,
                                    onValueChange = onTargetLongitudeChange,
                                    label = { Text("Longitude") },
                                    placeholder = { Text("-122.4194") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Radius
            OutlinedTextField(
                value = radius,
                onValueChange = onRadiusChange,
                label = { Text("Detection Radius") },
                placeholder = { Text("100") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    Text("meters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                supportingText = {
                    Text(
                        "Node triggers when device is within this radius of the target location",
                        fontSize = 11.sp
                    )
                }
            )

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "The workflow will monitor your location in the background and execute when you enter the specified area.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    @Composable
    fun GoogleMapsView(
        latitude: Double,
        longitude: Double,
        onLocationSelected: (Double, Double) -> Unit
    ) {
        // Simple placeholder - in production, use Google Maps SDK or iframe
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()

                    // Load Google Maps with marker
                    val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body, html { margin: 0; padding: 0; height: 100%; }
                            #map { height: 100%; }
                        </style>
                    </head>
                    <body>
                        <iframe 
                            width="100%" 
                            height="100%" 
                            frameborder="0" 
                            style="border:0"
                            src="https://www.google.com/maps?q=$latitude,$longitude&z=15&output=embed"
                            allowfullscreen>
                        </iframe>
                    </body>
                    </html>
                """.trimIndent()

                    loadData(html, "text/html", "UTF-8")
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun WebAgentConfiguration(
        webPrompt: String,
        onWebPromptChange: (String) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = webPrompt,
                onValueChange = onWebPromptChange,
                label = { Text("Task Instructions") },
                placeholder = { Text("Describe what you want the AI to do...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                supportingText = {
                    Text(
                        "Describe the web task in natural language",
                        fontSize = 11.sp
                    )
                }
            )

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E5F5)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "The AI agent will navigate websites and perform tasks autonomously. Results will be available to subsequent nodes.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Examples card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Example Tasks:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Go to Amazon, search for 'wireless headphones', and get the price of the first result", fontSize = 11.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Check the weather on weather.com for San Francisco and extract the temperature", fontSize = 11.sp, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Navigate to Twitter, search for #AI, and get the top 3 tweet texts", fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
    }
