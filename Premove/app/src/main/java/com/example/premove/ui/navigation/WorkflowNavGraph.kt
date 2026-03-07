package com.example.premove.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.premove.ui.workflows.WorkflowEditor
import com.example.premove.ui.home.Home
import com.example.premove.ui.nodes.NodeEditor
import com.example.premove.viewModel.WorkflowEditorViewModel
import com.example.premove.viewModel.WorkflowViewModel

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

        val workflowViewModel : WorkflowViewModel = hiltViewModel()
        val workflowEditorViewModel: WorkflowEditorViewModel = hiltViewModel()

        WorkflowEditor(workflowId, workflowViewModel, workflowEditorViewModel, onNodeClick = {
            nodeId ->
            navController.navigate(Route.NodeEditor.route + "/$nodeId")
        },onDelete = {
            navController.popBackStack()
        },onWorkflowConfigOpen={
            workflowId ->
            navController.navigate(Route.WorkflowConfig.route + "/$workflowId")
        }
            )
    }

    composable(
        route = Route.NodeEditor.route + "/{nodeId}"
    ) {
            backStackEntry ->
        val nodeId = backStackEntry.arguments
            ?.getString("nodeId")?.toIntOrNull()

        requireNotNull(nodeId) { "Node id is required" }

        var parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Route.WorkflowEditor.route + "/workflowId")
        }

        val workflowEditorViewModel: WorkflowEditorViewModel = hiltViewModel(parentEntry)

        NodeEditor(nodeId, workflowEditorViewModel, onBack = {navController.popBackStack()})
    }

    composable(
        route = Route.WorkflowConfig.route + "/{workflowId}"
    ) {
            backStackEntry ->
        val workflowId = backStackEntry.arguments
            ?.getString("workflowId")

        requireNotNull(workflowId) { "workflowId id is required" }

        var parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Route.WorkflowEditor.route + "/workflowId")
        }

        val workflowEditorViewModel: WorkflowEditorViewModel = hiltViewModel(parentEntry)

        Text("hello")
    }
}
