package com.example.premove.ui.navigation

sealed class Route(val route: String) {

    object Home : Route("home")

    object WorkflowEditor : Route("workflow_editor")
}
