package com.example.premove.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.premove.ui.workflows.WorkflowEditor
import com.example.premove.ui.home.Home
import com.example.premove.ui.nodes.NodeEditor

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
        route = Route.WorkflowEditor.route + "/{workflowId}"
    ) { backStackEntry ->
        val workflowId = backStackEntry.arguments
            ?.getString("workflowId")

        requireNotNull(workflowId) { "Workflow id is required" }

        WorkflowEditor(workflowId, onNodeClick = {
            nodeId ->
            navController.navigate(Route.NodeEditor.route + "/$nodeId")
        })
    }

    composable(
        route = Route.NodeEditor.route + "/{nodeId}"
    ) {
            backStackEntry ->
        val nodeId = backStackEntry.arguments
            ?.getInt("nodeId")

        requireNotNull(nodeId) { "Node id is required" }

        NodeEditor(nodeId)
    }
}
