package com.tannazetm.dailytasktracker.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
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
    private TextView dateText, immediateCount, completedCount;
    private HomeViewModel viewModel;
    private Calendar selectedCalendar = Calendar.getInstance();
    private Handler mainHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize handler for UI operations
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        initializeViews(root);
        setupRecyclerView();
        setupSwipeToDelete();
        setupObservers();
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

    private void setupObservers() {
        // Observe tasks - automatic UI updates
        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.setTasks(tasks);
            recyclerView.scrollToPosition(0);
        });

        // Observe counters
        viewModel.getImmediateCount().observe(getViewLifecycleOwner(), count ->
                immediateCount.setText(count + "/5")
        );

        viewModel.getCompletedCount().observe(getViewLifecycleOwner(), count ->
                completedCount.setText(String.valueOf(count))
        );

        // Observe toast messages
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showToast(message);
            }
        });

        // Observe task completion dialog trigger
        viewModel.getTaskToShowCompletion().observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                showTaskCompletionDialog(task);
            }
        });
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
    }
}





//package com.tannazetm.dailytasktracker.ui.home;
//
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.chip.Chip;
//import com.google.android.material.chip.ChipGroup;
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.tannazetm.dailytasktracker.R;
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskAdapter;
//import com.tannazetm.dailytasktracker.TaskDatabase;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.Executors;
//
//public class HomeFragment extends Fragment implements TaskAdapter.OnTaskInteractionListener {
//
//    private RecyclerView recyclerView;
//    private TaskAdapter adapter;
//    private ExtendedFloatingActionButton addTaskFab;
//    private TextView dateText, immediateCount, completedCount;
//    private TaskDatabase database;
//    private List<Task> taskList = new ArrayList<>();
//    private Calendar selectedCalendar = Calendar.getInstance();
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_home, container, false);
//
//        database = TaskDatabase.getDatabase(getContext());
//
//        initializeViews(root);
//        setupRecyclerView();
//        setupSwipeToDelete();
//        updateDate();
//        loadTasksFromDatabase();
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
//                Task taskToDelete = taskList.get(position);
//
//                new AlertDialog.Builder(getContext())
//                        .setTitle("Delete Task")
//                        .setMessage("Are you sure you want to delete \"" + taskToDelete.getTitle() + "\"?")
//                        .setPositiveButton("Delete", (dialog, which) -> {
//                            Executors.newSingleThreadExecutor().execute(() -> {
//                                database.taskDao().deleteTask(taskToDelete);
//                                if (getActivity() != null) {
//                                    getActivity().runOnUiThread(() -> {
//                                        taskList.remove(position);
//                                        adapter.notifyItemRemoved(position);
//                                        updateCounters();
//                                        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
//                                    });
//                                }
//                            });
//                        })
//                        .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(position))
//                        .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
//                        .show();
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
//    private void loadTasksFromDatabase() {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            List<Task> tasks = database.taskDao().getTodayTasks();
//            if (getActivity() != null) {
//                getActivity().runOnUiThread(() -> {
//                    taskList.clear();
//                    taskList.addAll(tasks);
//                    adapter.setTasks(taskList);
//                    updateCounters();
//                });
//            }
//        });
//    }
//
//    private void updateCounters() {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            int immediateTaskCount = database.taskDao().getImmediateTaskCount();
//            int completedTaskCount = database.taskDao().getTodayCompletedCount();
//
//            if (getActivity() != null) {
//                getActivity().runOnUiThread(() -> {
//                    immediateCount.setText(immediateTaskCount + "/5");
//                    completedCount.setText(String.valueOf(completedTaskCount));
//                });
//            }
//        });
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
//        builder.setPositiveButton("Create Task", null); // Set null to handle manually
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
//            // Calculate duration in minutes
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
//            Executors.newSingleThreadExecutor().execute(() -> {
//                long taskId = database.taskDao().insertTask(newTask);
//                newTask.setId((int) taskId);
//
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        taskList.add(0, newTask);
//                        adapter.notifyItemInserted(0);
//                        recyclerView.scrollToPosition(0);
//                        updateCounters();
//                        Toast.makeText(getContext(), "Task created successfully!", Toast.LENGTH_SHORT).show();
//                    });
//                }
//            });
//
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
//    // ===== TaskAdapter.OnTaskInteractionListener Implementation =====
//
//    @Override
//    public void onTaskCompleted(Task task) {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            database.taskDao().updateTask(task);
//            if (getActivity() != null) {
//                getActivity().runOnUiThread(() -> {
//                    updateCounters();
//
//                    // Show completion dialog if task was just completed and has tracking data
//                    if (task.isCompleted() && task.getActualDuration() > 0) {
//                        showTaskCompletionDialog(task);
//                    }
//                });
//            }
//        });
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
//            // Finished early
//            comparisonMessage.setText("Finished " + Math.abs(difference) + " minutes early! 🎉");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.completed));
//            differenceText.setText("-" + Math.abs(difference) + " min");
//            differenceText.setTextColor(getResources().getColor(R.color.completed));
//        } else if (difference < 0) {
//            // Took longer
//            comparisonMessage.setText("Took " + Math.abs(difference) + " minutes longer");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.overdue));
//            differenceText.setText("+" + Math.abs(difference) + " min");
//            differenceText.setTextColor(getResources().getColor(R.color.overdue));
//        } else {
//            // Exactly on time
//            comparisonMessage.setText("Perfectly on time! ⏰");
//            comparisonMessage.setTextColor(getResources().getColor(R.color.accent));
//            differenceText.setText("0 min");
//            differenceText.setTextColor(getResources().getColor(R.color.accent));
//        }
//
//        builder.setView(dialogView);
//        AlertDialog dialog = builder.create();
//
//        closeButton.setOnClickListener(v -> dialog.dismiss());
//
//        dialog.show();
//    }
//
//    @Override
//    public void onImmediateToggled(Task task) {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            int currentImmediateCount = database.taskDao().getImmediateTaskCount();
//            if (task.isImmediate() || currentImmediateCount < 5) {
//                database.taskDao().updateTask(task);
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(this::updateCounters);
//                }
//            } else {
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        task.setImmediate(false);
//                        adapter.notifyDataSetChanged();
//                        Toast.makeText(getContext(), "Maximum 5 immediate tasks allowed", Toast.LENGTH_SHORT).show();
//                    });
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onTaskDeleted(Task task) {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            database.taskDao().deleteTask(task);
//            if (getActivity() != null) {
//                getActivity().runOnUiThread(() -> {
//                    int position = taskList.indexOf(task);
//                    if (position != -1) {
//                        taskList.remove(position);
//                        adapter.notifyItemRemoved(position);
//                    }
//                    updateCounters();
//                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
//                });
//            }
//        });
//    }
//
//    @Override
//    public void onTaskEdit(Task task) {
//        // Show edit dialog (reuse enhanced dialog with pre-filled data)
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
//            Executors.newSingleThreadExecutor().execute(() -> {
//                database.taskDao().updateTask(task);
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        adapter.notifyDataSetChanged();
//                        Toast.makeText(getContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
//                    });
//                }
//            });
//
//            dialog.dismiss();
//        });
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
//
//                                task.setDueTimestamp(newCalendar.getTimeInMillis());
//                                task.setScheduledDate(newCalendar.getTimeInMillis());
//
//                                Executors.newSingleThreadExecutor().execute(() -> {
//                                    database.taskDao().updateTask(task);
//                                    if (getActivity() != null) {
//                                        getActivity().runOnUiThread(() -> {
//                                            adapter.notifyDataSetChanged();
//                                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//                                            Toast.makeText(getContext(),
//                                                    "Rescheduled to " + sdf.format(new Date(task.getDueTimestamp())),
//                                                    Toast.LENGTH_SHORT).show();
//                                        });
//                                    }
//                                });
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
//
//        datePickerDialog.show();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }
//}