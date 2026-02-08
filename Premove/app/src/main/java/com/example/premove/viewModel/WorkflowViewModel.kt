package com.example.premove.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.local.entity.WorkflowEntity
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

@HiltViewModel
class WorkflowViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository
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
    fun addWorkflow(title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch {
        workflowRepository.insertWorkflow(WorkflowEntity(id = UuidCreator.getTimeOrdered().toString(), title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
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