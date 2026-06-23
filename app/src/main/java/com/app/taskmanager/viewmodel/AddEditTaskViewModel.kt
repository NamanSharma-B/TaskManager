package com.app.taskmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.taskmanager.data.local.entity.Priority
import com.app.taskmanager.data.local.entity.Task
import com.app.taskmanager.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val taskId: Int = 0,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val titleError: String? = null,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false
)

sealed class AddEditEvent {
    object TaskSaved : AddEditEvent()
    data class ShowError(val message: String) : AddEditEvent()
}

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditEvent>()
    val events: SharedFlow<AddEditEvent> = _events.asSharedFlow()

    init {
        // Get taskId from navigation args (0 = new task)
        val taskId = savedStateHandle.get<Int>("taskId") ?: 0
        if (taskId != 0) {
            loadTask(taskId)
        }
    }

    private fun loadTask(taskId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val task = repository.getTaskById(taskId)
            task?.let {
                _uiState.update { state ->
                    state.copy(
                        taskId = it.id,
                        title = it.title,
                        description = it.description,
                        priority = it.priority,
                        dueDate = it.dueDate,
                        isCompleted = it.isCompleted,
                        isLoading = false,
                        isEditMode = true
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onPriorityChanged(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onDueDateChanged(timestamp: Long?) {
        _uiState.update { it.copy(dueDate = timestamp) }
    }

    fun onSaveTask() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val task = Task(
                    id = state.taskId,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    priority = state.priority,
                    dueDate = state.dueDate,
                    isCompleted = state.isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
                if (state.isEditMode) {
                    repository.updateTask(task)
                } else {
                    repository.insertTask(task)
                }
                _events.emit(AddEditEvent.TaskSaved)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(AddEditEvent.ShowError(e.message ?: "Something went wrong"))
            }
        }
    }
}
