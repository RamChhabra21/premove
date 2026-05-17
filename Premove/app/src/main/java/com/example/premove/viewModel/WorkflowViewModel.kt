package com.example.premove.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.local.entity.WorkflowEntity
import com.example.premove.data.local.entity.WorkflowRunEntity
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.example.premove.domain.model.NodeCategory
import com.example.premove.domain.model.NodeRegistry
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
import kotlinx.coroutines.flow.Flow
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

    fun getWorkflowRuns(workflowId: String): Flow<List<WorkflowRunEntity>> {
        return workflowRunRepository.getWorkflowRunsByWorkflowId(workflowId)
    }

    fun addWorkflow(id: String, title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch {
        workflowRepository.insertWorkflow(WorkflowEntity(id, title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
    }

    fun updateWorkflow(id: String, title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch{
        workflowRepository.updateWorkflow(WorkflowEntity(id = id, title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
    }

    fun updateWorkflowByObj(id: String, params: WorkflowUpdateParams) = viewModelScope.launch {
        val existing = workflowRepository.getWorkflowById(id)
        workflowRepository.updateWorkflow(
            existing.copy(
                title          = params.title          ?: existing.title,
                description    = params.description    ?: existing.description,
                isEnabled      = params.isEnabled      ?: existing.isEnabled,
                updatedAt      = params.updatedAt      ?: existing.updatedAt,
                timeoutMinutes = params.timeoutMinutes ?: existing.timeoutMinutes,
                maxRetries     = params.maxRetries     ?: existing.maxRetries,
                autoReset      = params.autoReset      ?: existing.autoReset
            )
        )
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
            val isInitial = initialnodeMap.containsKey(node.id)
            val isTrigger = NodeRegistry.getCategory(node.type) == NodeCategory.TRIGGER

            // Check if it's an initial node (no dependencies) AND NOT a trigger.
            // Trigger nodes should always stay PENDING until an external event fires them.
            val initialStatus = if (isInitial && !isTrigger) NodeStatus.READY else NodeStatus.PENDING

            nodeRunRepository.insertNodeRun(
                NodeRunEntity(
                    id = UuidCreator.getTimeOrdered().toString(),
                    workflowRunId = workflowRunId,
                    nodeId = node.id,
                    status = initialStatus,
                    inputCount = 0
                )
            )
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

data class WorkflowUpdateParams(
    val title: String? = null,
    val description: String? = null,
    val isEnabled: Boolean? = null,
    val updatedAt: Long? = null,
    val timeoutMinutes: Int? = null,
    val maxRetries: Int? = null,
    val autoReset: Boolean? = null
)