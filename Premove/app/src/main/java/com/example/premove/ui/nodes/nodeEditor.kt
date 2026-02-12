package com.example.premove.ui.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Node Type Definitions
enum class NodeType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    REASONING(
        "AI Reasoning",
        Icons.Default.CheckCircle,
        Color(0xFF9C27B0),
        "Process data with AI reasoning"
    ),
    WEB_REQUEST(
        "Web Request",
        Icons.Default.CheckCircle,
        Color(0xFF2196F3),
        "Make HTTP requests to APIs"
    ),
    ALARM(
        "Alarm",
        Icons.Default.CheckCircle,
        Color(0xFFF44336),
        "Set time-based triggers"
    ),
    LOCATION(
        "Location",
        Icons.Default.LocationOn,
        Color(0xFF4CAF50),
        "Location-based triggers"
    ),
    NOTIFICATION(
        "Notification",
        Icons.Default.Notifications,
        Color(0xFFFF9800),
        "Send notifications"
    ),
    CONDITION(
        "Condition",
        Icons.Default.CheckCircle,
        Color(0xFF00BCD4),
        "Conditional branching"
    ),
    DATA_TRANSFORM(
        "Transform Data",
        Icons.Default.CheckCircle,
        Color(0xFF673AB7),
        "Transform and process data"
    ),
    STORAGE(
        "Storage",
        Icons.Default.CheckCircle,
        Color(0xFF607D8B),
        "Store or retrieve data"
    ),
    EMAIL(
        "Email",
        Icons.Default.Email,
        Color(0xFFE91E63),
        "Send emails"
    ),
    DELAY(
        "Delay",
        Icons.Default.Search,
        Color(0xFF795548),
        "Add time delays"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeEditor(
    nodeId: Int,
    onBack: () -> Unit = {}
) {
    var nodeName by remember { mutableStateOf("My Node") }
    var selectedNodeType by remember { mutableStateOf(NodeType.REASONING) }
    var showTypeSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Node") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { /* Save */ }) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Node Name Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Node Name",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nodeName,
                            onValueChange = { nodeName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter node name") }
                        )
                    }
                }
            }

            // Node Type Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Node Type",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Selected Type Display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { showTypeSelector = !showTypeSelector }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    selectedNodeType.icon,
                                    contentDescription = null,
                                    tint = selectedNodeType.color,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        selectedNodeType.displayName,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        selectedNodeType.description,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Icon(
                                if (showTypeSelector) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                                contentDescription = null
                            )
                        }

                        // Type Selector Dropdown
                        if (showTypeSelector) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                )
                            ) {
                                Column {
                                    NodeType.values().forEach { type ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedNodeType = type
                                                    showTypeSelector = false
                                                }
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                type.icon,
                                                contentDescription = null,
                                                tint = type.color,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    type.displayName,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    type.description,
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            if (selectedNodeType == type) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = type.color
                                                )
                                            }
                                        }
                                        if (type != NodeType.values().last()) {
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Configuration Section (based on selected type)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = selectedNodeType.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Configuration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic configuration based on type
                        when (selectedNodeType) {
                            NodeType.REASONING -> ReasoningConfig()
                            NodeType.WEB_REQUEST -> WebRequestConfig()
                            NodeType.ALARM -> AlarmConfig()
                            NodeType.LOCATION -> LocationConfig()
                            NodeType.NOTIFICATION -> NotificationConfig()
                            NodeType.CONDITION -> ConditionConfig()
                            NodeType.DATA_TRANSFORM -> DataTransformConfig()
                            NodeType.STORAGE -> StorageConfig()
                            NodeType.EMAIL -> EmailConfig()
                            NodeType.DELAY -> DelayConfig()
                        }
                    }
                }
            }

            // Delete Node
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* Delete node */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Node")
                }
            }
        }
    }
}

// Configuration Components for Different Node Types

@Composable
fun ReasoningConfig() {
    var prompt by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("GPT-4") }
    var temperature by remember { mutableStateOf(0.7f) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            placeholder = { Text("Enter your reasoning prompt...") }
        )

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text("Temperature: ${String.format("%.1f", temperature)}", fontSize = 12.sp)
            Slider(
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0f..2f
            )
        }
    }
}

@Composable
fun WebRequestConfig() {
    var url by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("GET") }
    var headers by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://api.example.com/data") }
        )

        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = method,
                onValueChange = {},
                readOnly = true,
                label = { Text("Method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("GET", "POST", "PUT", "DELETE", "PATCH").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            method = option
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = headers,
            onValueChange = { headers = it },
            label = { Text("Headers (JSON)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            placeholder = { Text("{\n  \"Content-Type\": \"application/json\"\n}") }
        )

        if (method in listOf("POST", "PUT", "PATCH")) {
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Request Body") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("{\n  \"key\": \"value\"\n}") }
            )
        }
    }
}

@Composable
fun AlarmConfig() {
    var time by remember { mutableStateOf("09:00") }
    var repeatDaily by remember { mutableStateOf(false) }
    var daysOfWeek by remember { mutableStateOf(setOf<String>()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("09:00") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Repeat Daily")
            Switch(
                checked = repeatDaily,
                onCheckedChange = { repeatDaily = it }
            )
        }

        if (repeatDaily) {
            Text("Days of Week", fontSize = 12.sp, color = Color.Gray)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    FilterChip(
                        selected = daysOfWeek.contains(day),
                        onClick = {
                            daysOfWeek = if (daysOfWeek.contains(day)) {
                                daysOfWeek - day
                            } else {
                                daysOfWeek + day
                            }
                        },
                        label = { Text(day, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationConfig() {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("100") }
    var triggerType by remember { mutableStateOf("Enter") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        OutlinedTextField(
            value = radius,
            onValueChange = { radius = it },
            label = { Text("Radius (meters)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("Trigger When", fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = triggerType == "Enter",
                onClick = { triggerType = "Enter" },
                label = { Text("Enter Area") }
            )
            FilterChip(
                selected = triggerType == "Exit",
                onClick = { triggerType = "Exit" },
                label = { Text("Exit Area") }
            )
            FilterChip(
                selected = triggerType == "Both",
                onClick = { triggerType = "Both" },
                label = { Text("Both") }
            )
        }
    }
}

@Composable
fun NotificationConfig() {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Text("Priority", fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Low", "Normal", "High").forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick = { priority = p },
                    label = { Text(p) }
                )
            }
        }
    }
}

@Composable
fun ConditionConfig() {
    var leftOperand by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf("equals") }
    var rightOperand by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = leftOperand,
            onValueChange = { leftOperand = it },
            label = { Text("Left Value") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., {{previous_step.result}}") }
        )

        Text("Operator", fontSize = 12.sp, color = Color.Gray)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("equals", "not equals", ">", "<", ">=", "<=", "contains").forEach { op ->
                FilterChip(
                    selected = operator == op,
                    onClick = { operator = op },
                    label = { Text(op, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = rightOperand,
            onValueChange = { rightOperand = it },
            label = { Text("Right Value") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DataTransformConfig() {
    var inputPath by remember { mutableStateOf("") }
    var transformScript by remember { mutableStateOf("") }
    var outputFormat by remember { mutableStateOf("JSON") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = inputPath,
            onValueChange = { inputPath = it },
            label = { Text("Input Data Path") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("{{previous_step.data}}") }
        )

        OutlinedTextField(
            value = transformScript,
            onValueChange = { transformScript = it },
            label = { Text("Transform Script (JavaScript)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            placeholder = { Text("// Transform your data\nreturn data.map(item => item.value);") }
        )

        Text("Output Format", fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("JSON", "String", "Number", "Array").forEach { format ->
                FilterChip(
                    selected = outputFormat == format,
                    onClick = { outputFormat = format },
                    label = { Text(format) }
                )
            }
        }
    }
}

@Composable
fun StorageConfig() {
    var operation by remember { mutableStateOf("Store") }
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Operation", fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = operation == "Store",
                onClick = { operation = "Store" },
                label = { Text("Store Data") }
            )
            FilterChip(
                selected = operation == "Retrieve",
                onClick = { operation = "Retrieve" },
                label = { Text("Retrieve Data") }
            )
            FilterChip(
                selected = operation == "Delete",
                onClick = { operation = "Delete" },
                label = { Text("Delete Data") }
            )
        }

        OutlinedTextField(
            value = key,
            onValueChange = { key = it },
            label = { Text("Storage Key") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("my_data_key") }
        )

        if (operation == "Store") {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Data to store") }
            )
        }
    }
}

@Composable
fun EmailConfig() {
    var to by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = to,
            onValueChange = { to = it },
            label = { Text("To (Email Address)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            placeholder = { Text("recipient@example.com") }
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Email Body") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            placeholder = { Text("Your email content...") }
        )
    }
}

@Composable
fun DelayConfig() {
    var duration by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Seconds") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("Time Unit", fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Seconds", "Minutes", "Hours", "Days").forEach { u ->
                FilterChip(
                    selected = unit == u,
                    onClick = { unit = u },
                    label = { Text(u) }
                )
            }
        }
    }
}