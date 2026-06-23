package com.app.taskmanager.ui.addtask

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.taskmanager.R
import com.app.taskmanager.data.local.entity.Priority
import com.app.taskmanager.databinding.FragmentAddEditTaskBinding
import com.app.taskmanager.viewmodel.AddEditEvent
import com.app.taskmanager.viewmodel.AddEditTaskViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditTaskViewModel by viewModels()
    private val args: AddEditTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeUiState()
        observeEvents()
    }

    private fun setupViews() {
        binding.apply {
            editTextTitle.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.onTitleChanged(s.toString())
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            editTextDescription.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.onDescriptionChanged(s.toString())
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            radioGroupPriority.setOnCheckedChangeListener { _, checkedId ->
                val priority = when (checkedId) {
                    R.id.radioLow -> Priority.LOW
                    R.id.radioHigh -> Priority.HIGH
                    else -> Priority.MEDIUM
                }
                viewModel.onPriorityChanged(priority)
            }

            buttonPickDate.setOnClickListener {
                showDatePicker()
            }

            buttonClearDate.setOnClickListener {
                viewModel.onDueDateChanged(null)
            }

            buttonSave.setOnClickListener {
                viewModel.onSaveTask()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                viewModel.onDueDateChanged(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.apply {
                        if (!editTextTitle.hasFocus() && editTextTitle.text.toString() != state.title) {
                            editTextTitle.setText(state.title)
                        }
                        if (!editTextDescription.hasFocus() && editTextDescription.text.toString() != state.description) {
                            editTextDescription.setText(state.description)
                        }

                        textInputTitle.error = state.titleError

                        when (state.priority) {
                            Priority.LOW -> radioLow.isChecked = true
                            Priority.HIGH -> radioHigh.isChecked = true
                            else -> radioMedium.isChecked = true
                        }

                        state.dueDate?.let { timestamp ->
                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            textSelectedDate.text = sdf.format(Date(timestamp))
                            buttonClearDate.visibility = View.VISIBLE
                        } ?: run {
                            textSelectedDate.text = getString(R.string.no_date_selected)
                            buttonClearDate.visibility = View.GONE
                        }

                        requireActivity().title = if (state.isEditMode) getString(R.string.edit_task) else getString(R.string.add_task_val)
                        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddEditEvent.TaskSaved -> findNavController().popBackStack()
                        is AddEditEvent.ShowError ->
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
