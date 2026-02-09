package com.example.premove.viewModel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premove.data.local.entity.EdgeEntity
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.repository.EdgeRepository
import com.example.premove.domain.model.NodeLayoutType
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class WorkflowEditorViewModel @Inject constructor(
    private val nodeRepository: NodeRepository,
    private val edgeRepository: EdgeRepository
) : ViewModel(){

    private val _workflowId = MutableStateFlow<String?>(null);

    val nodes: StateFlow<List<NodeEntity>> = _workflowId
        .filterNotNull()
        .flatMapLatest {
            _workflowId ->
            nodeRepository.getNodesByWorkflowId(_workflowId)
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
    var localNodes = mutableStateOf<List<NodeData>>(emptyList())
    var localEdges = mutableStateOf<List<EdgeEntity>>(emptyList())

    private val positionUpdateJobs = mutableMapOf<Int, Job>()

    fun createEdge(edge: EdgeEntity) {
        viewModelScope.launch {
            edgeRepository.insertEdge(edge)
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
}