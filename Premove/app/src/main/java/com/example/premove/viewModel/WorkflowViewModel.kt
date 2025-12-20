package com.example.premove.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.premove.data.local.AppDatabase
import com.example.premove.model.WorkflowEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkflowViewModel(application: Application) : AndroidViewModel(application) {
    private val dao =
        AppDatabase.getDatabase(application).WorkflowDao()

    val searchQuery = MutableStateFlow("")

    private val _selectedDeleteWorkflowId = MutableStateFlow<Int?>(null)
    val selectedDeleteWorkflowId: StateFlow<Int?> = _selectedDeleteWorkflowId

    val workflows = dao.getAllWorkflows().stateIn(
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


    fun addWorkflow(title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch {
        dao.insertWorkflow(WorkflowEntity( title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
    }

    fun updateWorkflow(id: Int, title: String, description: String, isEnabled: Boolean, createdBy: Int) = viewModelScope.launch{
        dao.updateWorkflow(WorkflowEntity(id = id, title = title, description = description, isEnabled = isEnabled, createdBy = createdBy))
    }

    fun deleteWorkflow(workflowEntity: WorkflowEntity) = viewModelScope.launch{
        dao.deleteWorkflow(workflowEntity)
    }

    fun toggleWorkflow(workflowEntity: WorkflowEntity) {
        viewModelScope.launch {
            dao.updateWorkflow(workflowEntity.copy(isEnabled = !workflowEntity.isEnabled))
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onDeleteClicked(id: Int){
        _selectedDeleteWorkflowId.value = id
    }

    fun onDeleteDialogDismissed(){
        _selectedDeleteWorkflowId.value = null
    }
}