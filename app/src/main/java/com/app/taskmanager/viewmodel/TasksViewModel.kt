package com.app.taskmanager.viewmodel

import androidx.lifecycle.*
import com.app.taskmanager.data.local.entity.Task
import com.app.taskmanager.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter { ALL, ACTIVE, COMPLETED }
enum class SortOrder { DATE_CREATED, PRIORITY, TITLE }

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeCount: Int = 0,
    val filter: TaskFilter = TaskFilter.ALL,
    val sortOrder: SortOrder = SortOrder.DATE_CREATED,
    val searchQuery: String = ""
)

sealed class TaskEvent {
    data class ShowMessage(val message: String) : TaskEvent()
    data class NavigateToEdit(val task: Task) : TaskEvent()
    object NavigateToAddTask : TaskEvent()
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState(isLoading = true))
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskEvent>()
    val events: SharedFlow<TaskEvent> = _events.asSharedFlow()

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_CREATED)
    private val _searchQuery = MutableStateFlow("")

    init {
        loadTasks()
        observeActiveCount()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadTasks() {
        viewModelScope.launch {
            combine(_filter, _sortOrder, _searchQuery) { filter, sort, query ->
                Triple(filter, sort, query)
            }.flatMapLatest { (filter, sort, query) ->
                val flow = when {
                    query.isNotBlank() -> repository.searchTasks(query)
                    filter == TaskFilter.ACTIVE -> repository.getActiveTasks()
                    filter == TaskFilter.COMPLETED -> repository.getCompletedTasks()
                    else -> repository.getAllTasks()
                }
                flow.map { tasks ->
                    when (sort) {
                        SortOrder.PRIORITY -> tasks.sortedByDescending { it.priority.ordinal }
                        SortOrder.TITLE -> tasks.sortedBy { it.title }
                        else -> tasks
                    }
                }
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect { tasks ->
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            }
        }
    }

    private fun observeActiveCount() {
        viewModelScope.launch {
            repository.getActiveTaskCount().collect { count ->
                _uiState.update { it.copy(activeCount = count) }
            }
        }
    }

    fun onFilterChanged(filter: TaskFilter) {
        _filter.value = filter
        _uiState.update { it.copy(filter = filter) }
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
        _uiState.update { it.copy(sortOrder = sortOrder) }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTaskCheckedChanged(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskCompletion(task.id, isCompleted)
            val msg = if (isCompleted) "Task completed!" else "Task marked active"
            _events.emit(TaskEvent.ShowMessage(msg))
        }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _events.emit(TaskEvent.ShowMessage("Task deleted"))
        }
    }

    fun onDeleteAllCompleted() {
        viewModelScope.launch {
            repository.deleteAllCompletedTasks()
            _events.emit(TaskEvent.ShowMessage("Completed tasks cleared"))
        }
    }

    fun onAddTaskClicked() {
        viewModelScope.launch {
            _events.emit(TaskEvent.NavigateToAddTask)
        }
    }

    fun onEditTaskClicked(task: Task) {
        viewModelScope.launch {
            _events.emit(TaskEvent.NavigateToEdit(task))
        }
    }
}
