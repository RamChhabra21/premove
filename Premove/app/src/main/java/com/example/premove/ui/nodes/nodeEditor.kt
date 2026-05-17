package com.example.premove.ui.nodes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.premove.auth.slack.SlackAuth
import com.example.premove.domain.model.IntegrationDefinition
import com.example.premove.domain.model.NodeCategory
import com.example.premove.domain.model.NodeDefinition
import com.example.premove.domain.model.NodeRegistry
import com.example.premove.viewModel.WorkflowEditorViewModel
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeEditor(
    nodeId: Int,
    viewModel: WorkflowEditorViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val slackAuth = viewModel.slackAuth
    var isSlackConnected by remember { mutableStateOf(slackAuth.isConnected()) }

    // Refresh connection status when returning to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isSlackConnected = slackAuth.isConnected()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var nodeName by remember { mutableStateOf("") }
    var nodeDescription by remember { mutableStateOf("") } // User-defined description
    var selectedIntegrationId by remember { mutableStateOf<String?>(null) }
    var selectedNodeType by remember { mutableStateOf<NodeDefinition?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }
    
    var integrationDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    // Configuration states (General purpose)
    var field1 by remember { mutableStateOf("") } 
    var field2 by remember { mutableStateOf("") } 
    var field3 by remember { mutableStateOf("") } 

    val slackUsers by viewModel.slackUsers.collectAsState()

    LaunchedEffect(nodeId) {
        val node = viewModel.getNodeForEditor(nodeId)
        nodeName = node.title
        val definition = NodeRegistry.getDefinition(node.type)
        selectedNodeType = definition
        selectedIntegrationId = definition?.integration

        node.configJson?.let { json ->
            try {
                // Simple parser for basic fields
                val jsonObj = JSONObject(json)
                when (node.type.uppercase()) {
                    "AI_REASONING", "WEB_AGENT" -> field1 = jsonObj.optString("prompt", "")
                    "SLACK_MESSAGE_RECEIVED" -> field1 = jsonObj.optString("fromUser", "")
                    "SLACK_SEND_MESSAGE" -> {
                        field1 = jsonObj.optString("channel", "")
                        field2 = jsonObj.optString("message", "")
                    }
                    "SHEETS_APPEND_ROW" -> {
                        field1 = jsonObj.optString("spreadsheetId", "")
                        field2 = jsonObj.optString("values", "")
                    }
                    "CONFLUENCE_CREATE_PAGE" -> {
                        field1 = jsonObj.optString("spaceKey", "")
                        field2 = jsonObj.optString("title", "")
                        field3 = jsonObj.optString("content", "")
                    }
                    "WAIT_DELAY" -> field1 = jsonObj.optString("duration", "")
                    "WAIT_UNTIL" -> field1 = jsonObj.optString("time", "")
                    "SPEAK_TEXT" -> field1 = jsonObj.optString("text", "")
                }
            } catch (e: Exception) {}
        }
        isLoaded = true
    }

    LaunchedEffect(selectedIntegrationId) {
        if (selectedIntegrationId == "SLACK") {
            viewModel.fetchSlackUsers()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedNodeType == null) "New Node" else "Configure Node") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = {
                    if (selectedNodeType != null && isLoaded) {
                        TextButton(onClick = {
                            val config = when (selectedNodeType?.type?.uppercase()) {
                                "AI_REASONING", "WEB_AGENT" -> """{"prompt":"$field1"}"""
                                "SLACK_MESSAGE_RECEIVED" -> """{"fromUser":"$field1"}"""
                                "SLACK_SEND_MESSAGE" -> """{"channel":"$field1","message":"$field2"}"""
                                "SHEETS_APPEND_ROW" -> """{"spreadsheetId":"$field1","values":"$field2"}"""
                                "CONFLUENCE_CREATE_PAGE" -> """{"spaceKey":"$field1","title":"$field2","content":"$field3"}"""
                                "WAIT_DELAY" -> """{"duration":"$field1"}"""
                                "WAIT_UNTIL" -> """{"time":"$field1"}"""
                                "SPEAK_TEXT" -> """{"text":"$field1"}"""
                                else -> "{}"
                            }
                            viewModel.saveNode(nodeId, nodeName, selectedNodeType!!.type, config)
                            onBack()
                        }) {
                            Icon(Icons.Default.Check, null, tint = Color.White)
                            Text("SAVE", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        if (!isLoaded) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // 1. INTEGRATION DROPDOWN
                item {
                    Text("Service / Integration", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    
                    val selectedIntegration = NodeRegistry.getIntegration(selectedIntegrationId ?: "")
                    
                    ExposedDropdownMenuBox(
                        expanded = integrationDropdownExpanded,
                        onExpandedChange = { integrationDropdownExpanded = !integrationDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedIntegration?.name ?: "Choose a service...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = integrationDropdownExpanded) },
                            leadingIcon = { 
                                selectedIntegration?.let {
                                    if (it.iconRes != null) {
                                        Image(painterResource(it.iconRes), null, modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(it.icon, null, tint = it.color, modifier = Modifier.size(20.dp))
                                    }
                                } ?: Icon(Icons.Default.Apps, null) 
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = integrationDropdownExpanded,
                            onDismissRequest = { integrationDropdownExpanded = false }
                        ) {
                            NodeRegistry.integrations.forEach { integration ->
                                DropdownMenuItem(
                                    text = { Text(integration.name) },
                                    leadingIcon = { 
                                        if (integration.iconRes != null) {
                                            Image(painterResource(integration.iconRes), null, modifier = Modifier.size(20.dp))
                                        } else {
                                            Icon(integration.icon, null, tint = integration.color, modifier = Modifier.size(20.dp))
                                        }
                                    },
                                    onClick = {
                                        selectedIntegrationId = integration.id
                                        if (selectedNodeType?.integration != integration.id) {
                                            selectedNodeType = null // reset node type if integration changed
                                        }
                                        integrationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. NODE TYPE DROPDOWN
                if (selectedIntegrationId != null) {
                    item {
                        Text("Action or Trigger", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(4.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = typeDropdownExpanded,
                            onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedNodeType?.displayName ?: "Choose action...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                                leadingIcon = { 
                                    selectedNodeType?.let { 
                                        if (it.iconRes != null) {
                                            Image(painterResource(it.iconRes), null, modifier = Modifier.size(20.dp))
                                        } else if (it.icon.isNotEmpty()) {
                                            Text(it.icon)
                                        } else {
                                            Icon(Icons.Default.Settings, null, modifier = Modifier.size(20.dp))
                                        }
                                    } ?: Icon(Icons.Default.Search, null) 
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                val nodes = NodeRegistry.getDefinitionsByIntegration(selectedIntegrationId!!)
                                nodes.forEach { def ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(def.displayName, fontWeight = FontWeight.Medium)
                                            }
                                        },
                                        leadingIcon = {
                                            if (def.iconRes != null) {
                                                Image(painterResource(def.iconRes), null, modifier = Modifier.size(20.dp))
                                            } else if (def.icon.isNotEmpty()) {
                                                Text(def.icon)
                                            } else {
                                                Icon(Icons.Default.Settings, null, modifier = Modifier.size(20.dp))
                                            }
                                        },
                                        trailingIcon = {
                                            CategoryBadge(def.category)
                                        },
                                        onClick = {
                                            selectedNodeType = def
                                            typeDropdownExpanded = false
                                            if (nodeName.isEmpty() || nodeName == "New Node") nodeName = def.displayName
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. NODE DESCRIPTION BOX (About)
                if (selectedNodeType != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = selectedNodeType!!.accentColor.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, selectedNodeType!!.accentColor.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, null, tint = selectedNodeType!!.accentColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("About this node", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = selectedNodeType!!.accentColor)
                                    Spacer(Modifier.height(4.dp))
                                    Text(selectedNodeType!!.description, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }

                // Slack Authentication UI
                if (selectedIntegrationId == "SLACK") {
                    item {
                        SlackAuthSection(
                            isConnected = isSlackConnected,
                            onConnect = { slackAuth.launch() },
                            onDisconnect = { 
                                slackAuth.disconnect()
                                isSlackConnected = false
                            }
                        )
                    }
                }

                // 4. NODE LABEL
                if (selectedNodeType != null) {
                    item {
                        OutlinedTextField(
                            value = nodeName,
                            onValueChange = { nodeName = it },
                            label = { Text("Node Label") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // 5. CONFIGURATION SETTINGS
                if (selectedNodeType != null) {
                    item {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        
                        when (selectedNodeType?.type?.uppercase()) {
                            "AI_REASONING" -> {
                                ConfigurationField(label = "Reasoning Instructions", value = field1, onValueChange = { field1 = it }, placeholder = "Describe what the AI should think about...", isTextArea = true)
                            }
                            "WEB_AGENT" -> {
                                ConfigurationField(label = "Agent Task", value = field1, onValueChange = { field1 = it }, placeholder = "Go to amazon.com and find...", isTextArea = true)
                            }
                            "SLACK_MESSAGE_RECEIVED" -> {
                                SlackUserSelector(
                                    selectedUserId = field1,
                                    users = slackUsers,
                                    onUserSelected = { field1 = it }
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("Trigger only if message is from this user.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                            }
                            "SLACK_SEND_MESSAGE" -> {
                                ConfigurationField(label = "Channel ID / Name", value = field1, onValueChange = { field1 = it }, placeholder = "#general")
                                Spacer(Modifier.height(12.dp))
                                ConfigurationField(label = "Message Text", value = field2, onValueChange = { field2 = it }, placeholder = "Hello from AI!", isTextArea = true)
                            }
                            "SHEETS_APPEND_ROW" -> {
                                ConfigurationField(label = "Spreadsheet ID", value = field1, onValueChange = { field1 = it }, placeholder = "id_from_url")
                                Spacer(Modifier.height(12.dp))
                                ConfigurationField(label = "Values (comma separated)", value = field2, onValueChange = { field2 = it }, placeholder = "val1, val2, val3")
                            }
                            "CONFLUENCE_CREATE_PAGE" -> {
                                ConfigurationField(label = "Space Key", value = field1, onValueChange = { field1 = it }, placeholder = "TEAM")
                                Spacer(Modifier.height(12.dp))
                                ConfigurationField(label = "Page Title", value = field2, onValueChange = { field2 = it }, placeholder = "AI Summary")
                                Spacer(Modifier.height(12.dp))
                                ConfigurationField(label = "Page Content", value = field3, onValueChange = { field3 = it }, placeholder = "Body text...", isTextArea = true)
                            }
                            "WAIT_DELAY" -> {
                                ConfigurationField(label = "Duration (e.g. 30s, 5m, 1h)", value = field1, onValueChange = { field1 = it }, placeholder = "10m")
                            }
                            "WAIT_UNTIL" -> {
                                ConfigurationField(label = "Time (24h format HH:mm)", value = field1, onValueChange = { field1 = it }, placeholder = "09:00")
                            }
                            "SPEAK_TEXT" -> {
                                ConfigurationField(label = "Announcement Content", value = field1, onValueChange = { field1 = it }, placeholder = "What should be spoken aloud?", isTextArea = true)
                                Spacer(Modifier.height(4.dp))
                                Text("If this node follows an AI node, it will speak the AI's response instead.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                            }
                            else -> {
                                if (selectedNodeType?.category == NodeCategory.TRIGGER) {
                                    Text("This trigger will fire automatically when the event occurs.", color = Color.Gray, fontSize = 13.sp)
                                } else {
                                    Text("No extra configuration needed.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                            Text("Remove Node", Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Node?") },
                text = { Text("This will permanently remove this node from your workflow.") },
                confirmButton = { Button(onClick = { viewModel.deleteNode(nodeId); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlackUserSelector(
    selectedUserId: String,
    users: List<JSONObject>,
    onUserSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val selectedUser = users.find { it.optString("id") == selectedUserId }
    val filteredUsers = remember(searchQuery, users) {
        if (searchQuery.isEmpty()) users
        else users.filter { 
            it.optString("real_name").contains(searchQuery, ignoreCase = true) || 
            it.optString("name").contains(searchQuery, ignoreCase = true) 
        }
    }

    Column {
        Text("From Specific User (Optional)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedUser != null) {
                    val profile = selectedUser.optJSONObject("profile")
                    AsyncImage(
                        model = profile?.optString("image_192"),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(selectedUser.optString("real_name"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("@${selectedUser.optString("name")}", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    Icon(Icons.Default.Person, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text(if (selectedUserId.isEmpty()) "Any User" else selectedUserId, color = if (selectedUserId.isEmpty()) Color.Gray else Color.Black)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
    }

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.fillMaxHeight(0.8f).padding(16.dp)) {
                Text("Select Slack User", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or username...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                        }
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                LazyColumn(Modifier.weight(1f)) {
                    item {
                        ListItem(
                            headlineContent = { Text("Any User") },
                            leadingContent = { Icon(Icons.Default.People, null) },
                            modifier = Modifier.clickable { 
                                onUserSelected("")
                                expanded = false
                            }
                        )
                    }
                    
                    items(filteredUsers) { user ->
                        val profile = user.optJSONObject("profile")
                        ListItem(
                            headlineContent = { Text(user.optString("real_name")) },
                            supportingContent = { Text("@${user.optString("name")}") },
                            leadingContent = {
                                AsyncImage(
                                    model = profile?.optString("image_192"),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            },
                            modifier = Modifier.clickable {
                                onUserSelected(user.optString("id"))
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlackAuthSection(
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) Color(0xFFE8F5E9) else Color(0xFFF3E5F5)
        ),
        border = BorderStroke(1.dp, if (isConnected) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFF4A154B).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = null,
                tint = Color(0xFF4A154B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) "Slack Connected" else "Slack Not Connected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isConnected) Color(0xFF2E7D32) else Color(0xFF4A154B)
                )
                Text(
                    text = if (isConnected) "You can now receive and send messages." else "Sign in to grant permissions.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
            if (isConnected) {
                TextButton(onClick = onDisconnect) {
                    Text("Disconnect", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = { onConnect() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A154B)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConfigurationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isTextArea: Boolean = false
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = if (isTextArea) 4 else 1,
            maxLines = if (isTextArea) 8 else 1,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun CategoryBadge(category: NodeCategory) {
    val (color, bgColor) = when (category) {
        NodeCategory.TRIGGER -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        NodeCategory.ACTION -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
        NodeCategory.WAIT -> Color(0xFF795548) to Color(0xFFEFEBE9)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
        Text(category.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
