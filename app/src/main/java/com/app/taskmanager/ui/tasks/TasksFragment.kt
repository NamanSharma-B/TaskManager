package com.app.taskmanager.ui.tasks

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.taskmanager.R
import com.app.taskmanager.databinding.FragmentTasksBinding
import com.app.taskmanager.viewmodel.TaskEvent
import com.app.taskmanager.viewmodel.TaskFilter
import com.app.taskmanager.viewmodel.TasksViewModel
import com.app.taskmanager.viewmodel.SortOrder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasksViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        setupFilterChips()
        observeUiState()
        observeEvents()
        setupMenu()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskChecked = { task, isChecked ->
                viewModel.onTaskCheckedChanged(task, isChecked)
            },
            onTaskClick = { task ->
                viewModel.onEditTaskClicked(task)
            }
        )

        binding.recyclerView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // Swipe to delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = taskAdapter.currentList[viewHolder.adapterPosition]
                viewModel.onDeleteTask(task)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            viewModel.onAddTaskClicked()
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { viewModel.onFilterChanged(TaskFilter.ALL) }
        binding.chipActive.setOnClickListener { viewModel.onFilterChanged(TaskFilter.ACTIVE) }
        binding.chipCompleted.setOnClickListener { viewModel.onFilterChanged(TaskFilter.COMPLETED) }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    taskAdapter.submitList(state.tasks)
                    binding.textEmptyState.visibility =
                        if (state.tasks.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE
                    binding.progressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE
                    val suffix = if (state.activeCount != 1) "s" else ""
                    val text = getString(R.string.active_tasks,state.activeCount,suffix)
                    binding.textTaskCount.text = text
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is TaskEvent.ShowMessage ->
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        is TaskEvent.NavigateToAddTask ->
                            findNavController().navigate(
                                TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment()
                            )
                        is TaskEvent.NavigateToEdit ->
                            findNavController().navigate(
                                TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task.id)
                            )
                    }
                }
            }
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_tasks, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.onSearchQueryChanged(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort_date -> {
                        viewModel.onSortOrderChanged(SortOrder.DATE_CREATED); true
                    }
                    R.id.action_sort_priority -> {
                        viewModel.onSortOrderChanged(SortOrder.PRIORITY); true
                    }
                    R.id.action_sort_title -> {
                        viewModel.onSortOrderChanged(SortOrder.TITLE); true
                    }
                    R.id.action_delete_completed -> {
                        viewModel.onDeleteAllCompleted(); true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
