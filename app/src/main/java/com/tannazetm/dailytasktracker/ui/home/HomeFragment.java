package com.tannazetm.dailytasktracker.ui.home;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tannazetm.dailytasktracker.R;
import com.tannazetm.dailytasktracker.Task;
import com.tannazetm.dailytasktracker.TaskAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ExtendedFloatingActionButton addTaskFab;
    private TextView dateText, immediateCount, completedCount, activeFilterText;
    private HomeViewModel viewModel;
    private Calendar selectedCalendar = Calendar.getInstance();
    private Handler mainHandler;

    // Filter UI components
    private ImageButton filterToggleButton;
    private CardView filtersCard;
    private ChipGroup dateFilterChipGroup, statusFilterChipGroup, priorityFilterChipGroup;
    private View clearFiltersButton;
    private LinearLayout emptyStateLayout;

    // Filter state
    private boolean filtersVisible = false;

    // Handler for real-time tracking updates
    private Handler trackingUpdateHandler;
    private Runnable trackingUpdateRunnable;
    private static final int TRACKING_UPDATE_INTERVAL = 1000; // 1 second

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize handlers
        mainHandler = new Handler(Looper.getMainLooper());
        trackingUpdateHandler = new Handler(Looper.getMainLooper());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        initializeViews(root);
        setupRecyclerView();
        setupSwipeToDelete();
        setupFilterListeners();
        setupObservers();
        setupTrackingUpdates();
        updateDate();

        addTaskFab.setOnClickListener(v -> showEnhancedAddTaskDialog());

        return root;
    }

    private void initializeViews(View root) {
        recyclerView = root.findViewById(R.id.tasksRecyclerView);
        addTaskFab = root.findViewById(R.id.addTaskFab);
        dateText = root.findViewById(R.id.dateText);
        immediateCount = root.findViewById(R.id.immediateCount);
        completedCount = root.findViewById(R.id.completedCount);
        activeFilterText = root.findViewById(R.id.activeFilterText);
        emptyStateLayout = root.findViewById(R.id.emptyStateLayout);

        // Filter components
        filterToggleButton = root.findViewById(R.id.filterToggleButton);
        filtersCard = root.findViewById(R.id.filtersCard);
        dateFilterChipGroup = root.findViewById(R.id.dateFilterChipGroup);
        statusFilterChipGroup = root.findViewById(R.id.statusFilterChipGroup);
        priorityFilterChipGroup = root.findViewById(R.id.priorityFilterChipGroup);
        clearFiltersButton = root.findViewById(R.id.clearFiltersButton);
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    }

    private void setupFilterListeners() {
        // Toggle filters visibility
        filterToggleButton.setOnClickListener(v -> toggleFilters());

        // Date filters
        dateFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipToday) {
                viewModel.setDateFilter("today");
            } else if (checkedId == R.id.chipTomorrow) {
                viewModel.setDateFilter("tomorrow");
            } else if (checkedId == R.id.chipThisWeek) {
                viewModel.setDateFilter("week");
            } else if (checkedId == R.id.chipAllTasks) {
                viewModel.setDateFilter("all");
            } else if (checkedId == R.id.chipCustomDate) {
                showCustomDatePicker();
            }
            updateActiveFilterText();
        });

        // Status filters
        statusFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAllStatus) {
                viewModel.setStatusFilter("all");
            } else if (checkedId == R.id.chipActive) {
                viewModel.setStatusFilter("active");
            } else if (checkedId == R.id.chipCompleted) {
                viewModel.setStatusFilter("completed");
            } else if (checkedId == R.id.chipOverdue) {
                viewModel.setStatusFilter("overdue");
            }
            updateActiveFilterText();
        });

        // Priority filters
        priorityFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAllPriority) {
                viewModel.setPriorityFilter(-1);
            } else if (checkedId == R.id.chipHighPriority) {
                viewModel.setPriorityFilter(3);
            } else if (checkedId == R.id.chipMediumPriority) {
                viewModel.setPriorityFilter(2);
            } else if (checkedId == R.id.chipLowPriorityFilter) {
                viewModel.setPriorityFilter(1);
            }
            updateActiveFilterText();
        });

        // Clear filters
        clearFiltersButton.setOnClickListener(v -> clearAllFilters());
    }

    private void toggleFilters() {
        filtersVisible = !filtersVisible;

        if (filtersVisible) {
            filtersCard.setVisibility(View.VISIBLE);
            // Rotate icon
            ObjectAnimator.ofFloat(filterToggleButton, "rotation", 0f, 180f).start();
        } else {
            filtersCard.setVisibility(View.GONE);
            ObjectAnimator.ofFloat(filterToggleButton, "rotation", 180f, 0f).start();
        }
    }

    private void showCustomDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setCustomDateFilter(selection);
            updateActiveFilterText();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void clearAllFilters() {
        dateFilterChipGroup.check(R.id.chipToday);
        statusFilterChipGroup.check(R.id.chipAllStatus);
        priorityFilterChipGroup.check(R.id.chipAllPriority);
        viewModel.clearFilters();
        updateActiveFilterText();
        showToast("Filters cleared");
    }

    private void updateActiveFilterText() {
        String filterSummary = viewModel.getActiveFiltersSummary();
        if (filterSummary.isEmpty()) {
            activeFilterText.setVisibility(View.GONE);
        } else {
            activeFilterText.setVisibility(View.VISIBLE);
            activeFilterText.setText("Filters: " + filterSummary);
        }
    }

    private void setupObservers() {
        // Observe tasks - automatic UI updates
        viewModel.getFilteredTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks);

            // Show/hide empty state
            if (tasks.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.scrollToPosition(0);
            }
        });

        // Observe counters
        viewModel.getImmediateCount().observe(getViewLifecycleOwner(), count ->
                immediateCount.setText(count + "/5")
        );

        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count ->
                completedCount.setText(String.valueOf(count))
        );

        // Observe toast messages
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                String message = event.getContentIfNotHandled();
                if (message != null && !message.isEmpty()) {
                    showToast(message);
                }
            }
        });

        // Observe task completion dialog trigger
        viewModel.getTaskToShowCompletion().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                Task task = event.getContentIfNotHandled();
                if (task != null) {
                    showTaskCompletionDialog(task);
                }
            }
        });
    }

    // ISSUE #2 FIX: Setup real-time tracking updates
    private void setupTrackingUpdates() {
        trackingUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Notify adapter to refresh items that are in progress
                adapter.notifyDataSetChanged();

                // Schedule next update
                trackingUpdateHandler.postDelayed(this, TRACKING_UPDATE_INTERVAL);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start tracking updates when fragment is visible
        trackingUpdateHandler.postDelayed(trackingUpdateRunnable, TRACKING_UPDATE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop tracking updates when fragment is not visible
        trackingUpdateHandler.removeCallbacks(trackingUpdateRunnable);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION &&
                        adapter.getItemCount() > position) {
                    Task taskToDelete = adapter.getTaskAt(position);

                    if (taskToDelete != null) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Delete Task")
                                .setMessage("Are you sure you want to delete \"" + taskToDelete.getTitle() + "\"?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    viewModel.deleteTask(taskToDelete);
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    adapter.notifyItemChanged(position);
                                })
                                .setOnCancelListener(dialog -> {
                                    adapter.notifyItemChanged(position);
                                })
                                .show();
                    }
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        dateText.setText(sdf.format(new Date()));
    }

    private void showEnhancedAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_enhanced, null);

        // Find all views
        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.taskDescriptionInput);
        TextInputEditText hoursInput = dialogView.findViewById(R.id.durationHoursInput);
        TextInputEditText minutesInput = dialogView.findViewById(R.id.durationMinutesInput);
        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
        View dateTimeCard = dialogView.findViewById(R.id.dateTimeCard);
        ChipGroup durationChipGroup = dialogView.findViewById(R.id.durationChipGroup);
        ChipGroup priorityChipGroup = dialogView.findViewById(R.id.priorityChipGroup);

        // Initialize default values
        selectedCalendar = Calendar.getInstance();
        selectedCalendar.add(Calendar.HOUR, 1);
        updateDialogDateTime(selectedDateText, selectedTimeText);

        // Duration chip listeners
        durationChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip15min) {
                hoursInput.setText("0");
                minutesInput.setText("15");
            } else if (checkedId == R.id.chip30min) {
                hoursInput.setText("0");
                minutesInput.setText("30");
            } else if (checkedId == R.id.chip1hour) {
                hoursInput.setText("1");
                minutesInput.setText("0");
            } else if (checkedId == R.id.chip2hours) {
                hoursInput.setText("2");
                minutesInput.setText("0");
            }
        });

        // Date/Time picker
        dateTimeCard.setOnClickListener(v -> showDateTimePicker(selectedDateText, selectedTimeText));

        builder.setView(dialogView);
        builder.setPositiveButton("Create Task", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to prevent auto-dismiss
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String hoursStr = hoursInput.getText().toString().trim();
            String minutesStr = minutesInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError("Task name is required");
                return;
            }

            int hours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
            int minutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);
            int totalMinutes = (hours * 60) + minutes;

            if (totalMinutes == 0) {
                minutesInput.setError("Please set a duration");
                return;
            }

            // Get priority
            int priority = 2; // Default medium
            int selectedPriorityId = priorityChipGroup.getCheckedChipId();
            if (selectedPriorityId == R.id.chipLowPriority) priority = 1;
            else if (selectedPriorityId == R.id.chipHighPriority) priority = 3;

            // Create task
            Task newTask = new Task(name, description, selectedCalendar.getTimeInMillis(), totalMinutes);
            newTask.setPriority(priority);

            viewModel.insertTask(newTask);
            dialog.dismiss();
        });
    }

    private void showDateTimePicker(TextView dateText, TextView timeText) {
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                            (view1, hourOfDay, minute) -> {
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedCalendar.set(Calendar.MINUTE, minute);
                                updateDialogDateTime(dateText, timeText);
                            },
                            selectedCalendar.get(Calendar.HOUR_OF_DAY),
                            selectedCalendar.get(Calendar.MINUTE),
                            false);
                    timePicker.show();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH));

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void updateDialogDateTime(TextView dateText, TextView timeText) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        dateText.setText(dateFormat.format(selectedCalendar.getTime()));
        timeText.setText(timeFormat.format(selectedCalendar.getTime()));
    }

    private void showTaskCompletionDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_completion, null);

        TextView taskNameText = dialogView.findViewById(R.id.completedTaskName);
        TextView comparisonMessage = dialogView.findViewById(R.id.comparisonMessage);
        TextView estimatedTimeText = dialogView.findViewById(R.id.estimatedTimeText);
        TextView actualTimeText = dialogView.findViewById(R.id.actualTimeText);
        TextView differenceText = dialogView.findViewById(R.id.differenceText);
        View closeButton = dialogView.findViewById(R.id.closeButton);

        taskNameText.setText(task.getTitle());
        estimatedTimeText.setText(task.getFormattedDuration());
        actualTimeText.setText(task.getFormattedActualDuration());

        int difference = task.getEstimatedDuration() - task.getActualDuration();

        if (difference > 0) {
            comparisonMessage.setText("Finished " + Math.abs(difference) + " minutes early! 🎉");
            comparisonMessage.setTextColor(getResources().getColor(R.color.completed));
            differenceText.setText("-" + Math.abs(difference) + " min");
            differenceText.setTextColor(getResources().getColor(R.color.completed));
        } else if (difference < 0) {
            comparisonMessage.setText("Took " + Math.abs(difference) + " minutes longer");
            comparisonMessage.setTextColor(getResources().getColor(R.color.overdue));
            differenceText.setText("+" + Math.abs(difference) + " min");
            differenceText.setTextColor(getResources().getColor(R.color.overdue));
        } else {
            comparisonMessage.setText("Perfectly on time! ⏰");
            comparisonMessage.setTextColor(getResources().getColor(R.color.accent));
            differenceText.setText("0 min");
            differenceText.setTextColor(getResources().getColor(R.color.accent));
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showToast(String message) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            mainHandler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    // ===== TaskAdapter.OnTaskInteractionListener Implementation =====

    @Override
    public void onTaskCompleted(Task task) {
        viewModel.updateTask(task);
    }

    @Override
    public void onImmediateToggled(Task task) {
        viewModel.toggleTaskImmediate(task);
    }

    @Override
    public void onTaskDeleted(Task task) {
        viewModel.deleteTask(task);
    }

    @Override
    public void onTaskEdit(Task task) {
        showEditTaskDialog(task);
    }

    @Override
    public void onTaskReschedule(Task task) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.getDueTimestamp());

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            getContext(),
                            (timeView, hourOfDay, minute) -> {
                                Calendar newCalendar = Calendar.getInstance();
                                newCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                viewModel.rescheduleTask(task, newCalendar.getTimeInMillis());
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showEditTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_enhanced, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.taskDescriptionInput);
        TextInputEditText hoursInput = dialogView.findViewById(R.id.durationHoursInput);
        TextInputEditText minutesInput = dialogView.findViewById(R.id.durationMinutesInput);
        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
        View dateTimeCard = dialogView.findViewById(R.id.dateTimeCard);
        ChipGroup priorityChipGroup = dialogView.findViewById(R.id.priorityChipGroup);

        // Pre-fill data
        nameInput.setText(task.getTitle());
        descriptionInput.setText(task.getDescription());
        int hours = task.getEstimatedDuration() / 60;
        int minutes = task.getEstimatedDuration() % 60;
        hoursInput.setText(String.valueOf(hours));
        minutesInput.setText(String.valueOf(minutes));

        selectedCalendar.setTimeInMillis(task.getDueTimestamp());
        updateDialogDateTime(selectedDateText, selectedTimeText);

        // Set priority
        if (task.getPriority() == 1) priorityChipGroup.check(R.id.chipLowPriority);
        else if (task.getPriority() == 3) priorityChipGroup.check(R.id.chipHighPriority);
        else priorityChipGroup.check(R.id.chipMediumPriority);

        dateTimeCard.setOnClickListener(v -> showDateTimePicker(selectedDateText, selectedTimeText));

        builder.setView(dialogView);
        builder.setPositiveButton("Save Changes", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                nameInput.setError("Task name is required");
                return;
            }

            task.setTitle(name);
            task.setDescription(descriptionInput.getText().toString().trim());

            int newHours = Integer.parseInt(hoursInput.getText().toString().trim());
            int newMinutes = Integer.parseInt(minutesInput.getText().toString().trim());
            task.setEstimatedDuration((newHours * 60) + newMinutes);

            task.setDueTimestamp(selectedCalendar.getTimeInMillis());
            task.setScheduledDate(selectedCalendar.getTimeInMillis());

            int priority = 2;
            int selectedPriorityId = priorityChipGroup.getCheckedChipId();
            if (selectedPriorityId == R.id.chipLowPriority) priority = 1;
            else if (selectedPriorityId == R.id.chipHighPriority) priority = 3;
            task.setPriority(priority);

            viewModel.updateTask(task);
            dialog.dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any pending UI operations
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        if (trackingUpdateHandler != null) {
            trackingUpdateHandler.removeCallbacks(trackingUpdateRunnable);
        }
    }
}






//package com.tannazetm.dailytasktracker.ui.home;
//
//import android.animation.ObjectAnimator;
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.cardview.widget.CardView;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.chip.Chip;
//import com.google.android.material.chip.ChipGroup;
//import com.google.android.material.datepicker.MaterialDatePicker;
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.tannazetm.dailytasktracker.R;
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskAdapter;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class HomeFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {
//
//    private RecyclerView recyclerView;
//    private TaskAdapter adapter;
//    private ExtendedFloatingActionButton addTaskFab;
//    private TextView dateText, immediateCount, completedCount, activeFilterText;
//    private HomeViewModel viewModel;
//    private Calendar selectedCalendar = Calendar.getInstance();
//    private Handler mainHandler;
//
//    // Filter UI components
//    private ImageButton filterToggleButton;
//    private CardView filtersCard;
//    private ChipGroup dateFilterChipGroup, statusFilterChipGroup, priorityFilterChipGroup;
//    private View clearFiltersButton;
//    private LinearLayout emptyStateLayout;
//
//    // Filter state
//    private boolean filtersVisible = false;
//
//    // Handler for real-time tracking updates
//    private Handler trackingUpdateHandler;
//    private Runnable trackingUpdateRunnable;
//    private static final int TRACKING_UPDATE_INTERVAL = 1000; // 1 second
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_home, container, false);
//
//        // Initialize handlers
//        mainHandler = new Handler(Looper.getMainLooper());
//        trackingUpdateHandler = new Handler(Looper.getMainLooper());
//
//        // Initialize ViewModel
//        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
//
//        initializeViews(root);
//        setupRecyclerView();
//        setupSwipeToDelete();
//        setupFilterListeners();
//        setupObservers();
//        setupTrackingUpdates();
//        updateDate();
//
//        addTaskFab.setOnClickListener(v -> showEnhancedAddTaskDialog());
//
//        return root;
//    }
//
//    private void initializeViews(View root) {
//        recyclerView = root.findViewById(R.id.tasksRecyclerView);
//        addTaskFab = root.findViewById(R.id.addTaskFab);
//        dateText = root.findViewById(R.id.dateText);
//        immediateCount = root.findViewById(R.id.immediateCount);
//        completedCount = root.findViewById(R.id.completedCount);
//        activeFilterText = root.findViewById(R.id.activeFilterText);
//        emptyStateLayout = root.findViewById(R.id.emptyStateLayout);
//
//        // Filter components
//        filterToggleButton = root.findViewById(R.id.filterToggleButton);
//        filtersCard = root.findViewById(R.id.filtersCard);
//        dateFilterChipGroup = root.findViewById(R.id.dateFilterChipGroup);
//        statusFilterChipGroup = root.findViewById(R.id.statusFilterChipGroup);
//        priorityFilterChipGroup = root.findViewById(R.id.priorityFilterChipGroup);
//        clearFiltersButton = root.findViewById(R.id.clearFiltersButton);
//    }
//
//    private void setupRecyclerView() {
//        adapter = new TaskAdapter(getContext(), this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(adapter);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setItemViewCacheSize(20);
//        recyclerView.setDrawingCacheEnabled(true);
//        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//    }
//
//    private void setupFilterListeners() {
//        // Toggle filters visibility
//        filterToggleButton.setOnClickListener(v -> toggleFilters());
//
//        // Date filters
//        dateFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.chipToday) {
//                viewModel.setDateFilter("today");
//            } else if (checkedId == R.id.chipTomorrow) {
//                viewModel.setDateFilter("tomorrow");
//            } else if (checkedId == R.id.chipThisWeek) {
//                viewModel.setDateFilter("week");
//            } else if (checkedId == R.id.chipAllTasks) {
//                viewModel.setDateFilter("all");
//            } else if (checkedId == R.id.chipCustomDate) {
//                showCustomDatePicker();
//            }
//            updateActiveFilterText();
//        });
//
//        // Status filters
//        statusFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.chipAllStatus) {
//                viewModel.setStatusFilter("all");
//            } else if (checkedId == R.id.chipActive) {
//                viewModel.setStatusFilter("active");
//            } else if (checkedId == R.id.chipCompleted) {
//                viewModel.setStatusFilter("completed");
//            } else if (checkedId == R.id.chipOverdue) {
//                viewModel.setStatusFilter("overdue");
//            }
//            updateActiveFilterText();
//        });
//
//        // Priority filters
//        priorityFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.chipAllPriority) {
//                viewModel.setPriorityFilter(-1);
//            } else if (checkedId == R.id.chipHighPriority) {
//                viewModel.setPriorityFilter(3);
//            } else if (checkedId == R.id.chipMediumPriority) {
//                viewModel.setPriorityFilter(2);
//            } else if (checkedId == R.id.chipLowPriorityFilter) {
//                viewModel.setPriorityFilter(1);
//            }
//            updateActiveFilterText();
//        });
//
//        // Clear filters
//        clearFiltersButton.setOnClickListener(v -> clearAllFilters());
//    }
//
//    private void toggleFilters() {
//        filtersVisible = !filtersVisible;
//
//        if (filtersVisible) {
//            filtersCard.setVisibility(View.VISIBLE);
//            // Rotate icon
//            ObjectAnimator.ofFloat(filterToggleButton, "rotation", 0f, 180f).start();
//        } else {
//            filtersCard.setVisibility(View.GONE);
//            ObjectAnimator.ofFloat(filterToggleButton, "rotation", 180f, 0f).start();
//        }
//    }
//
//    private void showCustomDatePicker() {
//        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
//                .setTitleText("Select Date")
//                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
//                .build();
//
//        datePicker.addOnPositiveButtonClickListener(selection -> {
//            viewModel.setCustomDateFilter(selection);
//            updateActiveFilterText();
//        });
//
//        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
//    }
//
//    private void clearAllFilters() {
//        dateFilterChipGroup.check(R.id.chipToday);
//        statusFilterChipGroup.check(R.id.chipAllStatus);
//        priorityFilterChipGroup.check(R.id.chipAllPriority);
//        viewModel.clearFilters();
//        updateActiveFilterText();
//        showToast("Filters cleared");
//    }
//
//    private void updateActiveFilterText() {
//        String filterSummary = viewModel.getActiveFiltersSummary();
//        if (filterSummary.isEmpty()) {
//            activeFilterText.setVisibility(View.GONE);
//        } else {
//            activeFilterText.setVisibility(View.VISIBLE);
//            activeFilterText.setText("Filters: " + filterSummary);
//        }
//    }
//
//    private void setupObservers() {
//        // Observe tasks - automatic UI updates
//        viewModel.getFilteredTasks().observe(getViewLifecycleOwner(), tasks -> {
//            adapter.setTasks(tasks);
//
//            // Show/hide empty state
//            if (tasks.isEmpty()) {
//                emptyStateLayout.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            } else {
//                emptyStateLayout.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//                recyclerView.scrollToPosition(0);
//            }
//        });
//
//        // Observe counters
//        viewModel.getImmediateCount().observe(getViewLifecycleOwner(), count ->
//                immediateCount.setText(count + "/5")
//        );
//
//        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count ->
//                completedCount.setText(String.valueOf(count))
//        );
//
//        // Observe toast messages
//        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
//            if (message != null && !message.isEmpty()) {
//                showToast(message);
//            }
//        });
//
//        // Observe task completion dialog trigger
//        viewModel.getTaskToShowCompletion().observe(getViewLifecycleOwner(), task -> {
//            if (task != null) {
//                showTaskCompletionDialog(task);
//            }
//        });
//    }
//
//    // ISSUE #2 FIX: Setup real-time tracking updates
//    private void setupTrackingUpdates() {
//        trackingUpdateRunnable = new Runnable() {
//            @Override
//            public void run() {
//                // Notify adapter to refresh items that are in progress
//                adapter.notifyDataSetChanged();
//
//                // Schedule next update
//                trackingUpdateHandler.postDelayed(this, TRACKING_UPDATE_INTERVAL);
//            }
//        };
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Start tracking updates when fragment is visible
//        trackingUpdateHandler.postDelayed(trackingUpdateRunnable, TRACKING_UPDATE_INTERVAL);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        // Stop tracking updates when fragment is not visible
//        trackingUpdateHandler.removeCallbacks(trackingUpdateRunnable);
//    }
//
//    private void setupSwipeToDelete() {
//        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
//                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView,
//                                  @NonNull RecyclerView.ViewHolder viewHolder,
//                                  @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION &&
//                        adapter.getItemCount() > position) {
//                    Task taskToDelete = adapter.getTaskAt(position);
//
//                    if (taskToDelete != null) {
//                        new AlertDialog.Builder(getContext())
//                                .setTitle("Delete Task")
//                                .setMessage("Are you sure you want to delete \"" + taskToDelete.getTitle() + "\"?")
//                                .setPositiveButton("Delete", (dialog, which) -> {
//                                    viewModel.deleteTask(taskToDelete);
//                                })
//                                .setNegativeButton("Cancel", (dialog, which) -> {
//                                    adapter.notifyItemChanged(position);
//                                })
//                                .setOnCancelListener(dialog -> {
//                                    adapter.notifyItemChanged(position);
//                                })
//                                .show();
//                    }
//                }
//            }
//        };
//
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//    }
//
//    private void updateDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
//        dateText.setText(sdf.format(new Date()));
//    }
//
//    private void showEnhancedAddTaskDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_enhanced, null);
//
//        // Find all views
//        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
//        TextInputEditText descriptionInput = dialogView.findViewById(R.id.taskDescriptionInput);
//        TextInputEditText hoursInput = dialogView.findViewById(R.id.durationHoursInput);
//        TextInputEditText minutesInput = dialogView.findViewById(R.id.durationMinutesInput);
//        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
//        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
//        View dateTimeCard = dialogView.findViewById(R.id.dateTimeCard);
//        ChipGroup durationChipGroup = dialogView.findViewById(R.id.durationChipGroup);
//        ChipGroup priorityChipGroup = dialogView.findViewById(R.id.priorityChipGroup);
//
//        // Initialize default values
//        selectedCalendar = Calendar.getInstance();
//        selectedCalendar.add(Calendar.HOUR, 1);
//        updateDialogDateTime(selectedDateText, selectedTimeText);
//
//        // Duration chip listeners
//        durationChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.chip15min) {
//                hoursInput.setText("0");
//                minutesInput.setText("15");
//            } else if (checkedId == R.id.chip30min) {
//                hoursInput.setText("0");
//                minutesInput.setText("30");
//            } else if (checkedId == R.id.chip1hour) {
//                hoursInput.setText("1");
//                minutesInput.setText("0");
//            } else if (checkedId == R.id.chip2hours) {
//                hoursInput.setText("2");
//                minutesInput.setText("0");
//            }
//        });
//
//        // Date/Time picker
//        dateTimeCard.setOnClickListener(v -> showDateTimePicker(selectedDateText, selectedTimeText));
//
//        builder.setView(dialogView);
//        builder.setPositiveButton("Create Task", null);
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // Override positive button to prevent auto-dismiss
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
//            String name = nameInput.getText().toString().trim();
//            String description = descriptionInput.getText().toString().trim();
//            String hoursStr = hoursInput.getText().toString().trim();
//            String minutesStr = minutesInput.getText().toString().trim();
//
//            if (name.isEmpty()) {
//                nameInput.setError("Task name is required");
//                return;
//            }
//
//            int hours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
//            int minutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);
//            int totalMinutes = (hours * 60) + minutes;
//
//            if (totalMinutes == 0) {
//                minutesInput.setError("Please set a duration");
//                return;
//            }
//
//            // Get priority
//            int priority = 2; // Default medium
//            int selectedPriorityId = priorityChipGroup.getCheckedChipId();
//            if (selectedPriorityId == R.id.chipLowPriority) priority = 1;
//            else if (selectedPriorityId == R.id.chipHighPriority) priority = 3;
//
//            // Create task
//            Task newTask = new Task(name, description, selectedCalendar.getTimeInMillis(), totalMinutes);
//            newTask.setPriority(priority);
//
//            viewModel.insertTask(newTask);
//            dialog.dismiss();
//        });
//    }
//
//    private void showDateTimePicker(TextView dateText, TextView timeText) {
//        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
//                (view, year, month, dayOfMonth) -> {
//                    selectedCalendar.set(Calendar.YEAR, year);
//                    selectedCalendar.set(Calendar.MONTH, month);
//                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//
//                    TimePickerDialog timePicker = new TimePickerDialog(getContext(),
//                            (view1, hourOfDay, minute) -> {
//                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                                selectedCalendar.set(Calendar.MINUTE, minute);
//                                updateDialogDateTime(dateText, timeText);
//                            },
//                            selectedCalendar.get(Calendar.HOUR_OF_DAY),
//                            selectedCalendar.get(Calendar.MINUTE),
//                            false);
//                    timePicker.show();
//                },
//                selectedCalendar.get(Calendar.YEAR),
//                selectedCalendar.get(Calendar.MONTH),
//                selectedCalendar.get(Calendar.DAY_OF_MONTH));
//
//        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//        datePicker.show();
//    }
//
//    private void updateDialogDateTime(TextView dateText, TextView timeText) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
//        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
//
//        dateText.setText(dateFormat.format(selectedCalendar.getTime()));
//        timeText.setText(timeFormat.format(selectedCalendar.getTime()));
//    }
//
//    private void showTaskCompletionDialog(Task task) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_completion, null);
//
//        TextView taskNameText = dialogView.findViewById(R.id.completedTaskName);
//        TextView comparisonMessage = dialogView.findViewById(R.id.comparisonMessage);
//        TextView estimatedTimeText = dialogView.findViewById(R.id.estimatedTimeText);
//        TextView actualTimeText = dialogView.findViewById(R.id.actualTimeText);
//        TextView differenceText = dialogView.findViewById(R.id.differenceText);
//        View closeButton = dialogView.findViewById(R.id.closeButton);
//
//        taskNameText.setText(task.getTitle());
//        estimatedTimeText.setText(task.getFormattedDuration());
//        actualTimeText.setText(task.getFormattedActualDuration());
//
//        int difference = task.getEstimatedDuration() - task.getActualDuration();
//
//        if (difference > 0) {
//            comparisonMessage.setText("Finished " + Math.abs(difference) + " minutes early! 🎉");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.completed));
//            differenceText.setText("-" + Math.abs(difference) + " min");
//            differenceText.setTextColor(getResources().getColor(R.color.completed));
//        } else if (difference < 0) {
//            comparisonMessage.setText("Took " + Math.abs(difference) + " minutes longer");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.overdue));
//            differenceText.setText("+" + Math.abs(difference) + " min");
//            differenceText.setTextColor(getResources().getColor(R.color.overdue));
//        } else {
//            comparisonMessage.setText("Perfectly on time! ⏰");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.accent));
//            differenceText.setText("0 min");
//            differenceText.setTextColor(getResources().getColor(R.color.accent));
//        }
//
//        builder.setView(dialogView);
//        AlertDialog dialog = builder.create();
//        closeButton.setOnClickListener(v -> dialog.dismiss());
//        dialog.show();
//    }
//
//    private void showToast(String message) {
//        if (getActivity() != null && !getActivity().isFinishing()) {
//            mainHandler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
//        }
//    }
//
//    // ===== TaskAdapter.OnTaskInteractionListener Implementation =====
//
//    @Override
//    public void onTaskCompleted(Task task) {
//        viewModel.updateTask(task);
//    }
//
//    @Override
//    public void onImmediateToggled(Task task) {
//        viewModel.toggleTaskImmediate(task);
//    }
//
//    @Override
//    public void onTaskDeleted(Task task) {
//        viewModel.deleteTask(task);
//    }
//
//    @Override
//    public void onTaskEdit(Task task) {
//        showEditTaskDialog(task);
//    }
//
//    @Override
//    public void onTaskReschedule(Task task) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(task.getDueTimestamp());
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                getContext(),
//                (view, year, month, dayOfMonth) -> {
//                    TimePickerDialog timePickerDialog = new TimePickerDialog(
//                            getContext(),
//                            (timeView, hourOfDay, minute) -> {
//                                Calendar newCalendar = Calendar.getInstance();
//                                newCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
//                                viewModel.rescheduleTask(task, newCalendar.getTimeInMillis());
//                            },
//                            calendar.get(Calendar.HOUR_OF_DAY),
//                            calendar.get(Calendar.MINUTE),
//                            false
//                    );
//                    timePickerDialog.show();
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//        );
//        datePickerDialog.show();
//    }
//
//    private void showEditTaskDialog(Task task) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_enhanced, null);
//
//        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
//        TextInputEditText descriptionInput = dialogView.findViewById(R.id.taskDescriptionInput);
//        TextInputEditText hoursInput = dialogView.findViewById(R.id.durationHoursInput);
//        TextInputEditText minutesInput = dialogView.findViewById(R.id.durationMinutesInput);
//        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
//        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
//        View dateTimeCard = dialogView.findViewById(R.id.dateTimeCard);
//        ChipGroup priorityChipGroup = dialogView.findViewById(R.id.priorityChipGroup);
//
//        // Pre-fill data
//        nameInput.setText(task.getTitle());
//        descriptionInput.setText(task.getDescription());
//        int hours = task.getEstimatedDuration() / 60;
//        int minutes = task.getEstimatedDuration() % 60;
//        hoursInput.setText(String.valueOf(hours));
//        minutesInput.setText(String.valueOf(minutes));
//
//        selectedCalendar.setTimeInMillis(task.getDueTimestamp());
//        updateDialogDateTime(selectedDateText, selectedTimeText);
//
//        // Set priority
//        if (task.getPriority() == 1) priorityChipGroup.check(R.id.chipLowPriority);
//        else if (task.getPriority() == 3) priorityChipGroup.check(R.id.chipHighPriority);
//        else priorityChipGroup.check(R.id.chipMediumPriority);
//
//        dateTimeCard.setOnClickListener(v -> showDateTimePicker(selectedDateText, selectedTimeText));
//
//        builder.setView(dialogView);
//        builder.setPositiveButton("Save Changes", null);
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
//            String name = nameInput.getText().toString().trim();
//            if (name.isEmpty()) {
//                nameInput.setError("Task name is required");
//                return;
//            }
//
//            task.setTitle(name);
//            task.setDescription(descriptionInput.getText().toString().trim());
//
//            int newHours = Integer.parseInt(hoursInput.getText().toString().trim());
//            int newMinutes = Integer.parseInt(minutesInput.getText().toString().trim());
//            task.setEstimatedDuration((newHours * 60) + newMinutes);
//
//            task.setDueTimestamp(selectedCalendar.getTimeInMillis());
//            task.setScheduledDate(selectedCalendar.getTimeInMillis());
//
//            int priority = 2;
//            int selectedPriorityId = priorityChipGroup.getCheckedChipId();
//            if (selectedPriorityId == R.id.chipLowPriority) priority = 1;
//            else if (selectedPriorityId == R.id.chipHighPriority) priority = 3;
//            task.setPriority(priority);
//
//            viewModel.updateTask(task);
//            dialog.dismiss();
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Clean up any pending UI operations
//        if (mainHandler != null) {
//            mainHandler.removeCallbacksAndMessages(null);
//        }
//        if (trackingUpdateHandler != null) {
//            trackingUpdateHandler.removeCallbacks(trackingUpdateRunnable);
//        }
//    }
//}



















//package com.tannazetm.dailytasktracker.ui.home;
//
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.chip.ChipGroup;
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.tannazetm.dailytasktracker.R;
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskAdapter;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class HomeFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {
//
//    private RecyclerView recyclerView;
//    private TaskAdapter adapter;
//    private ExtendedFloatingActionButton addTaskFab;
//    private TextView dateText, immediateCount, completedCount;
//    private HomeViewModel viewModel;
//    private Calendar selectedCalendar = Calendar.getInstance();
//    private Handler mainHandler;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_home, container, false);
//
//        // Initialize handler for UI operations
//        mainHandler = new Handler(Looper.getMainLooper());
//
//        // Initialize ViewModel
//        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
//
//        initializeViews(root);
//        setupRecyclerView();
//        setupSwipeToDelete();
//        setupObservers();
//        updateDate();
//
//        addTaskFab.setOnClickListener(v -> showEnhancedAddTaskDialog());
//
//        return root;
//    }
//
//    private void initializeViews(View root) {
//        recyclerView = root.findViewById(R.id.tasksRecyclerView);
//        addTaskFab = root.findViewById(R.id.addTaskFab);
//        dateText = root.findViewById(R.id.dateText);
//        immediateCount = root.findViewById(R.id.immediateCount);
//        completedCount = root.findViewById(R.id.completedCount);
//    }
//
//    private void setupRecyclerView() {
//        adapter = new TaskAdapter(getContext(), this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setAdapter(adapter);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setItemViewCacheSize(20);
//        recyclerView.setDrawingCacheEnabled(true);
//        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//    }
//
//    private void setupObservers() {
//        // Observe tasks - automatic UI updates
//        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
//            adapter.setTasks(tasks);
//            recyclerView.scrollToPosition(0);
//        });
//
//        // Observe counters
//        viewModel.getImmediateCount().observe(getViewLifecycleOwner(), count ->
//                immediateCount.setText(count + "/5")
//        );
//
//        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count ->
//                completedCount.setText(String.valueOf(count))
//        );
//
//        // Observe toast messages
//        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
//            if (message != null && !message.isEmpty()) {
//                showToast(message);
//            }
//        });
//
//        // Observe task completion dialog trigger
//        viewModel.getTaskToShowCompletion().observe(getViewLifecycleOwner(), task -> {
//            if (task != null) {
//                showTaskCompletionDialog(task);
//            }
//        });
//    }
//
//    private void setupSwipeToDelete() {
//        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
//                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView,
//                                  @NonNull RecyclerView.ViewHolder viewHolder,
//                                  @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION &&
//                        adapter.getItemCount() > position) {
//                    Task taskToDelete = adapter.getTaskAt(position);
//
//                    if (taskToDelete != null) {
//                        new AlertDialog.Builder(getContext())
//                                .setTitle("Delete Task")
//                                .setMessage("Are you sure you want to delete \"" + taskToDelete.getTitle() + "\"?")
//                                .setPositiveButton("Delete", (dialog, which) -> {
//                                    viewModel.deleteTask(taskToDelete);
//                                })
//                                .setNegativeButton("Cancel", (dialog, which) -> {
//                                    adapter.notifyItemChanged(position);
//                                })
//                                .setOnCancelListener(dialog -> {
//                                    adapter.notifyItemChanged(position);
//                                })
//                                .show();
//                    }
//                }
//            }
//        };
//
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//    }
//
//    private void updateDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
//        dateText.setText(sdf.format(new Date()));
//    }
//
//    private void showEnhancedAddTaskDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_enhanced, null);
//
//        // Find all views
//        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
//        TextInputEditText descriptionInput = dialogView.findViewById(R.id.taskDescriptionInput);
//        TextInputEditText hoursInput = dialogView.findViewById(R.id.durationHoursInput);
//        TextInputEditText minutesInput = dialogView.findViewById(R.id.durationMinutesInput);
//        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
//        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
//        View dateTimeCard = dialogView.findViewById(R.id.dateTimeCard);
//        ChipGroup durationChipGroup = dialogView.findViewById(R.id.durationChipGroup);
//        ChipGroup priorityChipGroup = dialogView.findViewById(R.id.priorityChipGroup);
//
//        // Initialize default values
//        selectedCalendar = Calendar.getInstance();
//        selectedCalendar.add(Calendar.HOUR, 1);
//        updateDialogDateTime(selectedDateText, selectedTimeText);
//
//        // Duration chip listeners
//        durationChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (checkedId == R.id.chip15min) {
//                hoursInput.setText("0");
//                minutesInput.setText("15");
//            } else if (checkedId == R.id.chip30min) {
//                hoursInput.setText("0");
//                minutesInput.setText("30");
//            } else if (checkedId == R.id.chip1hour) {
//                hoursInput.setText("1");
//                minutesInput.setText("0");
//            } else if (checkedId == R.id.chip2hours) {
//                hoursInput.setText("2");
//                minutesInput.setText("0");
//            }
//        });
//
//        // Date/Time picker
//        dateTimeCard.setOnClickListener(v -> showDateTimePicker(selectedDateText, selectedTimeText));
//
//        builder.setView(dialogView);
//        builder.setPositiveButton("Create Task", null);
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // Override positive button to prevent auto-dismiss
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
//            String name = nameInput.getText().toString().trim();
//            String description = descriptionInput.getText().toString().trim();
//            String hoursStr = hoursInput.getText().toString().trim();
//            String minutesStr = minutesInput.getText().toString().trim();
//
//            if (name.isEmpty()) {
//                nameInput.setError("Task name is required");
//                return;
//            }
//
//            int hours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
//            int minutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);
//            int totalMinutes = (hours * 60) + minutes;
//
//            if (totalMinutes == 0) {
//                minutesInput.setError("Please set a duration");
//                return;
//            }
//
//            // Get priority
//            int priority = 2; // Default medium
//            int selectedPriorityId = priorityChipGroup.getCheckedChipId();
//            if (selectedPriorityId == R.id.chipLowPriority) priority = 1;
//            else if (selectedPriorityId == R.id.chipHighPriority) priority = 3;
//
//            // Create task
//            Task newTask = new Task(name, description, selectedCalendar.getTimeInMillis(), totalMinutes);
//            newTask.setPriority(priority);
//
//            viewModel.insertTask(newTask);
//            dialog.dismiss();
//        });
//    }
//
//    private void showDateTimePicker(TextView dateText, TextView timeText) {
//        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
//                (view, year, month, dayOfMonth) -> {
//                    selectedCalendar.set(Calendar.YEAR, year);
//                    selectedCalendar.set(Calendar.MONTH, month);
//                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//
//                    TimePickerDialog timePicker = new TimePickerDialog(getContext(),
//                            (view1, hourOfDay, minute) -> {
//                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                                selectedCalendar.set(Calendar.MINUTE, minute);
//                                updateDialogDateTime(dateText, timeText);
//                            },
//                            selectedCalendar.get(Calendar.HOUR_OF_DAY),
//                            selectedCalendar.get(Calendar.MINUTE),
//                            false);
//                    timePicker.show();
//                },
//                selectedCalendar.get(Calendar.YEAR),
//                selectedCalendar.get(Calendar.MONTH),
//                selectedCalendar.get(Calendar.DAY_OF_MONTH));
//
//        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//        datePicker.show();
//    }
//
//    private void updateDialogDateTime(TextView dateText, TextView timeText) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
//        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
//
//        dateText.setText(dateFormat.format(selectedCalendar.getTime()));
//        timeText.setText(timeFormat.format(selectedCalendar.getTime()));
//    }
//
//    private void showTaskCompletionDialog(Task task) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_completion, null);
//
//        TextView taskNameText = dialogView.findViewById(R.id.completedTaskName);
//        TextView comparisonMessage = dialogView.findViewById(R.id.comparisonMessage);
//        TextView estimatedTimeText = dialogView.findViewById(R.id.estimatedTimeText);
//        TextView actualTimeText = dialogView.findViewById(R.id.actualTimeText);
//        TextView differenceText = dialogView.findViewById(R.id.differenceText);
//        View closeButton = dialogView.findViewById(R.id.closeButton);
//
//        taskNameText.setText(task.getTitle());
//        estimatedTimeText.setText(task.getFormattedDuration());
//        actualTimeText.setText(task.getFormattedActualDuration());
//
//        int difference = task.getEstimatedDuration() - task.getActualDuration();
//
//        if (difference > 0) {
//            comparisonMessage.setText("Finished " + Math.abs(difference) + " minutes early! 🎉");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.completed));
//            differenceText.setText("-" + Math.abs(difference) + " min");
//            differenceText.setTextColor(getResources().getColor(R.color.completed));
//        } else if (difference < 0) {
//            comparisonMessage.setText("Took " + Math.abs(difference) + " minutes longer");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.overdue));
//            differenceText.setText("+" + Math.abs(difference) + " min");
//            differenceText.setTextColor(getResources().getColor(R.color.overdue));
//        } else {
//            comparisonMessage.setText("Perfectly on time! ⏰");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.accent));
//            differenceText.setText("0 min");
//            differenceText.setTextColor(getResources().getColor(R.color.accent));
//        }
//
//        builder.setView(dialogView);
//        AlertDialog dialog = builder.create();
//        closeButton.setOnClickListener(v -> dialog.dismiss());
//        dialog.show();
//    }
//
//    private void showToast(String message) {
//        if (getActivity() != null && !getActivity().isFinishing()) {
//            mainHandler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
//        }
//    }
//
//    // ===== TaskAdapter.OnTaskInteractionListener Implementation =====
//
//    @Override
//    public void onTaskCompleted(Task task) {
//        viewModel.updateTask(task);
//    }
//
//    @Override
//    public void onImmediateToggled(Task task) {
//        viewModel.toggleTaskImmediate(task);
//    }
//
//    @Override
//    public void onTaskDeleted(Task task) {
//        viewModel.deleteTask(task);
//    }
//
//    @Override
//    public void onTaskEdit(Task task) {
//        showEditTaskDialog(task);
//    }
//
//    @Override
//    public void onTaskReschedule(Task task) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(task.getDueTimestamp());
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                getContext(),
//                (view, year, month, dayOfMonth) -> {
//                    TimePickerDialog timePickerDialog = new TimePickerDialog(
//                            getContext(),
//                            (timeView, hourOfDay, minute) -> {
//                                Calendar newCalendar = Calendar.getInstance();
//                                newCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
//                                viewModel.rescheduleTask(task, newCalendar.getTimeInMillis());
//                            },
//                            calendar.get(Calendar.HOUR_OF_DAY),
//                            calendar.get(Calendar.MINUTE),
//                            false
//                    );
//                    timePickerDialog.show();
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//        );
//        datePickerDialog.show();
//    }
//
//    private void showEditTaskDialog(Task task) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task_enhanced, null);
//
//        TextInputEditText nameInput = dialogView.findViewById(R.id.taskNameInput);
//        TextInputEditText descriptionInput = dialogView.findViewById(R.id.taskDescriptionInput);
//        TextInputEditText hoursInput = dialogView.findViewById(R.id.durationHoursInput);
//        TextInputEditText minutesInput = dialogView.findViewById(R.id.durationMinutesInput);
//        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);
//        TextView selectedTimeText = dialogView.findViewById(R.id.selectedTimeText);
//        View dateTimeCard = dialogView.findViewById(R.id.dateTimeCard);
//        ChipGroup priorityChipGroup = dialogView.findViewById(R.id.priorityChipGroup);
//
//        // Pre-fill data
//        nameInput.setText(task.getTitle());
//        descriptionInput.setText(task.getDescription());
//        int hours = task.getEstimatedDuration() / 60;
//        int minutes = task.getEstimatedDuration() % 60;
//        hoursInput.setText(String.valueOf(hours));
//        minutesInput.setText(String.valueOf(minutes));
//
//        selectedCalendar.setTimeInMillis(task.getDueTimestamp());
//        updateDialogDateTime(selectedDateText, selectedTimeText);
//
//        // Set priority
//        if (task.getPriority() == 1) priorityChipGroup.check(R.id.chipLowPriority);
//        else if (task.getPriority() == 3) priorityChipGroup.check(R.id.chipHighPriority);
//        else priorityChipGroup.check(R.id.chipMediumPriority);
//
//        dateTimeCard.setOnClickListener(v -> showDateTimePicker(selectedDateText, selectedTimeText));
//
//        builder.setView(dialogView);
//        builder.setPositiveButton("Save Changes", null);
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
//            String name = nameInput.getText().toString().trim();
//            if (name.isEmpty()) {
//                nameInput.setError("Task name is required");
//                return;
//            }
//
//            task.setTitle(name);
//            task.setDescription(descriptionInput.getText().toString().trim());
//
//            int newHours = Integer.parseInt(hoursInput.getText().toString().trim());
//            int newMinutes = Integer.parseInt(minutesInput.getText().toString().trim());
//            task.setEstimatedDuration((newHours * 60) + newMinutes);
//
//            task.setDueTimestamp(selectedCalendar.getTimeInMillis());
//            task.setScheduledDate(selectedCalendar.getTimeInMillis());
//
//            int priority = 2;
//            int selectedPriorityId = priorityChipGroup.getCheckedChipId();
//            if (selectedPriorityId == R.id.chipLowPriority) priority = 1;
//            else if (selectedPriorityId == R.id.chipHighPriority) priority = 3;
//            task.setPriority(priority);
//
//            viewModel.updateTask(task);
//            dialog.dismiss();
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Clean up any pending UI operations
//        if (mainHandler != null) {
//            mainHandler.removeCallbacksAndMessages(null);
//        }
//    }
//}
//
//
