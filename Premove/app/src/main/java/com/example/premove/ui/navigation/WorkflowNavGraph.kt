package com.example.premove.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.premove.ui.workflows.WorkflowEditor
import com.example.premove.ui.home.Home

fun NavGraphBuilder.workflowNavGraph(
    navController: NavController
) {
    composable(Route.Home.route) {
        Home(
            onWorkflowClick = {
                workflowId ->
                navController.navigate(Route.WorkflowEditor.route + "/$workflowId")
            }
        )
    }

    composable(
        route = Route.WorkflowEditor.route + "/{id}"
    ) { backStackEntry ->
        val id = backStackEntry.arguments
            ?.getString("id")

        requireNotNull(id) { "Workflow id is required" }

        WorkflowEditor(id)
    }
}
