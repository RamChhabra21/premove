package com.example.premove.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premove.data.local.entity.EdgeEntity
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.data.repository.EdgeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.domain.model.NodeLayoutType
import com.example.premove.ui.nodes.NodeStatus
import com.example.premove.ui.workflows.NodeData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class WorkflowEditorViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository,
    private val nodeRepository: NodeRepository,
    private val nodeRunRepository: NodeRunRepository,
    private val edgeRepository: EdgeRepository
) : ViewModel(){

    private val _workflowId = MutableStateFlow<String?>(null);

    val nodes: StateFlow<List<NodeEntity>> = _workflowId
        .filterNotNull()
        .flatMapLatest {
            _workflowId ->
            nodeRepository.observeNodesByWorkflowId(_workflowId)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val edges: StateFlow<List<EdgeEntity>> = _workflowId
        .filterNotNull()
        .flatMapLatest {
            _workflowId ->
            edgeRepository.getEdgesByWorkflowId(_workflowId)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // NEW: Runtime status for latest run
    private val latestRunStatus: StateFlow<List<NodeRunEntity>> = _workflowId
        .filterNotNull()
        .flatMapLatest { workflowId ->
            nodeRunRepository.getLatestNodeRunByWorkflowId(workflowId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val nodesWithStatus: StateFlow<List<NodeWithStatus>> = combine(
        nodes,
        latestRunStatus
    ) { compileNodes, runtimeNodes ->
        val runtimeMap = runtimeNodes.associateBy { it.nodeId }
        compileNodes.map { node ->
            NodeWithStatus(
                id = node.id,
                workflowId = node.workflowId,
                title = node.title,
                type = node.type,
                x = node.x,
                y = node.y,
                layoutType = node.layoutType,
                configJson = node.configJson,
                status = runtimeMap[node.id]?.status ?: NodeStatus.PENDING
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var localNodes = mutableStateOf<List<NodeData>>(emptyList())
    var localEdges = mutableStateOf<List<EdgeEntity>>(emptyList())

    private val positionUpdateJobs = mutableMapOf<Int, Job>()

    fun deleteWorkflow(workflowId: String) = viewModelScope.launch{
        workflowRepository.deleteWorkflow(workflowId)
    }

    fun createEdge(edge: EdgeEntity) {
        viewModelScope.launch {
            edgeRepository.insertEdge(edge)
        }
    }

    fun updateEdge(edge: EdgeEntity) {
        viewModelScope.launch {
            edgeRepository.updateEdge(
                edge
            )
        }
    }

    fun setWorkflowId(workflowId: String){
        _workflowId.value = workflowId
    }

    fun createNode(
        title: String,
        type: String,
        position: Offset = Offset(100f, 100f),
        configJson: String? = null
    ){
        viewModelScope.launch {
            val workflowId = _workflowId.value ?: return@launch

            val newNode = NodeEntity(
                // id = 0 (default), Room auto-generates
                workflowId = workflowId,
                title = title,
                type = type,
                x = position.x,
                y = position.y,
                configJson = configJson,
                layoutType = NodeLayoutType.VERTICAL
            )

            nodeRepository.insertNode(newNode)
        }
    }

    fun updateNodePositionDebounced(nodeId: Int, newPosition: Offset, delayMs: Long = 300){
        positionUpdateJobs[nodeId]?.cancel()
        positionUpdateJobs[nodeId] = viewModelScope.launch {
            delay(delayMs)
            nodeRepository.updateNodePosition(nodeId, newPosition)
        }
    }

    // ===== NodeEditor specific methods =====

    /**
     * Get node for editor - returns default node for new (-1) or loaded node for existing
     */
    suspend fun getNodeForEditor(nodeId: Int): NodeEntity {
        return withContext(Dispatchers.IO) {
            if (nodeId == -1) {
                // Return default new node
                NodeEntity(
                    id = 0, // Room will auto-generate on insert
                    workflowId = _workflowId.value ?: "",
                    title = "New Node",
                    type = "WEB_AGENT", // Default type
                    x = 0f,
                    y = 0f,
                    layoutType = NodeLayoutType.VERTICAL,
                    configJson = """{"prompt":""}"""
                )
            } else {
                // Load from database using Flow
                nodeRepository.getNodeById(nodeId) ?: NodeEntity(
                 id = 0,
                 workflowId = _workflowId.value ?: "",
                 title = "Node Not Found",
                 type = "WEB_AGENT",
                 x = 0f,
                 y = 0f,
                 layoutType = NodeLayoutType.VERTICAL,
                 configJson = "{}"
             )
            }
        }
    }

    suspend fun getNodeById(nodeId: Int): NodeEntity? {
        return nodeRepository.getNodeById(nodeId)
    }

    /**
     * Save node - insert if new (nodeId == -1 or 0), update if existing
     */
    fun saveNode(
        nodeId: Int,
        title: String,
        type: String,
        configJson: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (nodeId == -1 || nodeId == 0) {
                // Insert new node
                val newNode = NodeEntity(
                    id = 0, // Room auto-generates
                    workflowId = _workflowId.value ?: "",
                    title = title,
                    type = type,
                    x = 0f, // Default position, can be updated later
                    y = 0f,
                    layoutType = getLayoutTypeForNodeType(type),
                    configJson = configJson
                )
                nodeRepository.insertNode(newNode)
            } else {
                // Update existing node
                nodeRepository.updateNodeConfig(
                    nodeId = nodeId,
                    title = title,
                    type = type,
                    configJson = configJson
                )
            }
        }
    }

    /**
     * Delete node by ID
     */
    fun deleteNode(nodeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            nodeRepository.deleteNode(nodeId)
        }
    }

    /**
     * Helper to map node type to layout type
     */
    private fun getLayoutTypeForNodeType(type: String): NodeLayoutType {
        return when (type) {
            "ALARM" -> NodeLayoutType.VERTICAL
            "LOCATION" -> NodeLayoutType.VERTICAL
            "WEB_AGENT" -> NodeLayoutType.VERTICAL
            else -> NodeLayoutType.VERTICAL
        }
    }
}

data class NodeWithStatus(
    val id: Int,
    val workflowId: String,
    val title: String,
    val type: String,
    val x: Float,
    val y: Float,
    val layoutType: NodeLayoutType,
    val configJson: String?,
    val status: NodeStatus
)