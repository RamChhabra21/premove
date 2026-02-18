package com.example.premove.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.local.entity.WorkflowEntity
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.example.premove.ui.nodes.NodeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class WorkflowViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository,
    private val workflowRunRepository: WorkflowRunRepository,
    private val nodeRepository: NodeRepository,
    private val nodeRunRepository: NodeRunRepository
) : ViewModel(){
    val searchQuery = MutableStateFlow("")

    private val _selectedDeleteWorkflowId = MutableStateFlow<String?>(null)
    val selectedDeleteWorkflowId: StateFlow<String?> = _selectedDeleteWorkflowId

    val workflows = workflowRepository.getAllWorkflows().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val filteredWorkflows: StateFlow<List<WorkflowEntity>> =
        combine(searchQuery.debounce(300), workflows) { query, list ->
            // custom filtration logic here
            if (query.isBlank()) list
            else list.filter { it.title.contains(query, ignoreCase = true) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // workflow crud

    suspend fun getWorkflowById(workflowId: String): WorkflowEntity {
        return withContext(Dispatchers.IO) {
            workflowRepository.getWorkflowById(workflowId)
        }
    }

    fun addWorkflow(id: String, title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch {
        workflowRepository.insertWorkflow(WorkflowEntity(id, title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
    }

    fun updateWorkflow(id: String, title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch{
        workflowRepository.updateWorkflow(WorkflowEntity(id = id, title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
    }

    fun deleteWorkflow(workflowId: String) = viewModelScope.launch{
        workflowRepository.deleteWorkflow(workflowId)
    }

    fun toggleWorkflow(workflowId: String) {
        viewModelScope.launch {
            workflowRepository.toggleWorkflow(workflowId)
            initialiseWorkflow(workflowId)
        }
    }

    fun initialiseWorkflow(workflowId: String) = viewModelScope.launch {
        val workflowRunId = UuidCreator.getTimeOrdered().toString()
        // make a new workflow run and mark all nodes pending
        workflowRunRepository.insertWorkflowRun(workflowRunId, workflowId)
        // get all nodes
        val nodes = nodeRepository.getNodesByWorkflowId(workflowId)
        // intialize nodes
        val toInitialiseNodes = nodeRepository.getNodesToBeInitialised(workflowId)
        val initialnodeMap = toInitialiseNodes.associateBy { it.id }

        // generate nodeRuns for each node
        for (node in nodes) {
            if(initialnodeMap.containsKey(node.id)) {
                nodeRunRepository.insertNodeRun(
                    NodeRunEntity(
                        id = UuidCreator.getTimeOrdered().toString(),
                        workflowRunId = workflowRunId,
                        nodeId = node.id,
                        status = NodeStatus.READY
                    )
                )
            }
            else{
                nodeRunRepository.insertNodeRun(
                    NodeRunEntity(
                        id = UuidCreator.getTimeOrdered().toString(),
                        workflowRunId = workflowRunId,
                        nodeId = node.id,
                        status = NodeStatus.PENDING
                    )
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onDeleteClicked(id: String){
        _selectedDeleteWorkflowId.value = id
    }

    fun onDeleteDialogDismissed(){
        _selectedDeleteWorkflowId.value = null
    }
}