package com.example.premove.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.premove.R

data class IntegrationDefinition(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String,
    val iconRes: Int? = null
)

data class NodeDefinition(
    val type: String,
    val category: NodeCategory,
    val integration: String, // ID of the integration
    val displayName: String,
    val description: String = "",
    val icon: String = "⚙️",
    val vectorIcon: ImageVector = Icons.Default.Settings,
    val accentColor: Color = Color(0xFF607D8B),
    val iconRes: Int? = null
)

object NodeRegistry {
    val integrations = listOf(
        IntegrationDefinition("AI", "Intelligence", Icons.Default.AutoAwesome, Color(0xFFFF9800), "Reasoning and decision making"),
        IntegrationDefinition("WEB", "Web Agent", Icons.Default.Language, Color(0xFF2196F3), "Autonomous web automation"),
        IntegrationDefinition("SLACK", "Slack", Icons.Default.Forum, Color(0xFF4A154B), "Workspace communication", iconRes = R.drawable.slack_icon),
        IntegrationDefinition("GOOGLE_SHEETS", "Sheets", Icons.Default.TableChart, Color(0xFF0F9D58), "Data and spreadsheets"),
        IntegrationDefinition("ALARM", "Alarm", Icons.Default.Alarm, Color(0xFFF44336), "Time-based events"),
        IntegrationDefinition("WAIT", "Wait", Icons.Default.HourglassEmpty, Color(0xFF795548), "Flow control"),
        IntegrationDefinition("VOICE", "Voice Output", Icons.Default.VolumeUp, Color(0xFF673AB7), "Speak text aloud")
    )

    private val definitions = listOf(
        // --- AI ---
        NodeDefinition("AI_REASONING", NodeCategory.ACTION, "AI", "AI Reasoning", "Think, analyze, and decide the next steps using LLMs", "🧠", Icons.Default.Psychology, Color(0xFFFF9800)),

        // --- WEB ---
        NodeDefinition("WEB_AGENT", NodeCategory.ACTION, "WEB", "Web Agent", "Command an AI agent to perform tasks in a web browser", "🌐", Icons.Default.TravelExplore, Color(0xFF2196F3)),

        // --- SLACK ---
        NodeDefinition("SLACK_MESSAGE_RECEIVED", NodeCategory.TRIGGER, "SLACK", "Message Received", "Trigger when a message is posted to a channel", "", Icons.Default.ChatBubble, Color(0xFF4A154B), iconRes = R.drawable.slack_icon),
        NodeDefinition("SLACK_SEND_MESSAGE", NodeCategory.ACTION, "SLACK", "Send Message", "Post a new message or update to Slack", "", Icons.Default.Send, Color(0xFF4A154B), iconRes = R.drawable.slack_icon),

        // --- GOOGLE SHEETS ---
        NodeDefinition("SHEETS_ROW_ADDED", NodeCategory.TRIGGER, "GOOGLE_SHEETS", "Row Added", "Trigger when a new row is detected in a sheet", "📝", Icons.Default.Add, Color(0xFF0F9D58)),
        NodeDefinition("SHEETS_APPEND_ROW", NodeCategory.ACTION, "GOOGLE_SHEETS", "Append Row", "Add a new row of data to a spreadsheet", "➕", Icons.Default.PlaylistAdd, Color(0xFF0F9D58)),
        // --- ALARM ---
        NodeDefinition("ALARM_FIRED", NodeCategory.ACTION, "ALARM", "Alarm Fired", "Trigger at a specific time or scheduled alarm", "⏰", Icons.Default.Alarm, Color(0xFFF44336)),

        // --- WAIT ---
        NodeDefinition("WAIT_DELAY", NodeCategory.WAIT, "WAIT", "Wait Duration", "Pause the workflow for a fixed amount of time", "⏳", Icons.Default.Timer, Color(0xFF795548)),
        NodeDefinition("WAIT_UNTIL", NodeCategory.WAIT, "WAIT", "Wait Until", "Pause until a specific time of day occurs", "🕙", Icons.Default.AccessTime, Color(0xFF795548)),
        
        // --- VOICE ---
        NodeDefinition("SPEAK_TEXT", NodeCategory.ACTION, "VOICE", "Read Aloud", "Speak the provided text through the device speaker", "🔊", Icons.Default.RecordVoiceOver, Color(0xFF673AB7))
    )

    fun getIntegration(id: String) = integrations.find { it.id == id }

    fun getDefinition(type: String): NodeDefinition? {
        return definitions.find { it.type.uppercase() == type.uppercase() }
    }

    fun getDefinitionsByIntegration(integrationId: String): List<NodeDefinition> {
        return definitions.filter { it.integration == integrationId }
    }

    fun getCategory(type: String): NodeCategory {
        return getDefinition(type)?.category ?: NodeCategory.ACTION
    }

    fun getDisplayName(type: String): String {
        return getDefinition(type)?.displayName ?: type
    }

    fun getIcon(type: String): String {
        return getDefinition(type)?.icon ?: "⚙️"
    }

    fun getIconRes(type: String): Int? {
        return getDefinition(type)?.iconRes
    }

    fun getAccentColor(type: String): Color {
        return getDefinition(type)?.accentColor ?: Color(0xFF607D8B)
    }

    fun getAllDefinitions(): List<NodeDefinition> = definitions
    
    fun getDefinitionsByCategory(category: NodeCategory): List<NodeDefinition> {
        return definitions.filter { it.category == category }
    }
}
