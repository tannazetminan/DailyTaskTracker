package com.tannazetm.dailytasktracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tannazetm.dailytasktracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Use binding.navView instead of findViewById since we're using view binding
        BottomNavigationView navView = binding.navView;
        // Setup navigation - home, dashboard, and notifications fragments
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up repository when app is destroyed
        TaskRepository.getInstance(this).cleanup();
    }
}














// // old btut working
//package com.tannazetm.dailytasktracker;
//
//import android.app.Application;
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.android.material.chip.Chip;
//import com.google.android.material.chip.ChipGroup;
//import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
//import com.google.android.material.textfield.TextInputEditText;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskInteractionListener {
//
//    private RecyclerView recyclerView;
//    private TaskAdapter adapter;
//    private ExtendedFloatingActionButton addTaskFab;
//    private TextView dateText, immediateCount, completedCount;
//    private List<Task> taskList = new ArrayList<>();
//    private TaskDatabase database;
//    private Calendar selectedCalendar = Calendar.getInstance();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Initialize database
//        database = TaskDatabase.getDatabase(this);
//
//        initializeViews();
//        setupRecyclerView();
//        updateDate();
//        loadTasksFromDatabase();
//
//        addTaskFab.setOnClickListener(v -> showEnhancedAddTaskDialog());
//    }
//
//    private void initializeViews() {
//        recyclerView = findViewById(R.id.tasksRecyclerView);
//        addTaskFab = findViewById(R.id.addTaskFab);
//        dateText = findViewById(R.id.dateText);
//        immediateCount = findViewById(R.id.immediateCount);
//        completedCount = findViewById(R.id.completedCount);
//    }
//
//    private void setupRecyclerView() {
//        adapter = new TaskAdapter(this, this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//        recyclerView.setHasFixedSize(true);
//    }
//
//    private void updateDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
//        dateText.setText(sdf.format(new Date()));
//    }
//
//    private void loadTasksFromDatabase() {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            List<Task> tasks = database.taskDao().getAllTasks();
//            runOnUiThread(() -> {
//                taskList.clear();
//                taskList.addAll(tasks);
//                adapter.setTasks(taskList);
//                updateCounters();
//            });
//        });
//    }
//
//    private void updateCounters() {
//        int immediateTaskCount = 0;
//        int completedTaskCount = 0;
//
//        for (Task task : taskList) {
//            if (task.isImmediate() && !task.isCompleted()) {
//                immediateTaskCount++;
//            }
//            if (task.isCompleted()) {
//                completedTaskCount++;
//            }
//        }
//
//        immediateCount.setText(immediateTaskCount + "/5");
//        completedCount.setText(String.valueOf(completedTaskCount));
//    }
//
//    private void showEnhancedAddTaskDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task_enhanced, null);
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
//                runOnUiThread(() -> {
//                    loadTasksFromDatabase();
//                    showSafeToast("Task created successfully!");
//                });
//            });
//
//            dialog.dismiss();
//        });
//    }
//
//    private void showDateTimePicker(TextView dateText, TextView timeText) {
//        DatePickerDialog datePicker = new DatePickerDialog(this,
//                (view, year, month, dayOfMonth) -> {
//                    selectedCalendar.set(Calendar.YEAR, year);
//                    selectedCalendar.set(Calendar.MONTH, month);
//                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//
//                    TimePickerDialog timePicker = new TimePickerDialog(this,
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
//    // Safe Toast method to handle DeadObjectException
//    private void showSafeToast(String message) {
//        if (!isFinishing() && !isDestroyed()) {
//            try {
//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                // Ignore toast errors
//            }
//        }
//    }
//
//    @Override
//    public void onTaskCompleted(Task task) {
//        // Update task in database
//        Executors.newSingleThreadExecutor().execute(() -> {
//            database.taskDao().updateTask(task);
//            runOnUiThread(() -> {
//                updateCounters();
//            });
//        });
//    }
//
//    @Override
//    public void onImmediateToggled(Task task) {
//        // Check immediate count before updating
//        Executors.newSingleThreadExecutor().execute(() -> {
//            int currentImmediateCount = database.taskDao().getImmediateTaskCount();
//
//            if (!task.isImmediate() && currentImmediateCount >= 5) {
//                runOnUiThread(() -> {
//                    task.setImmediate(false);  // Revert the change
//                    showSafeToast("Maximum 5 immediate tasks allowed");
//                    adapter.notifyDataSetChanged();
//                });
//            } else {
//                // Save the change
//                database.taskDao().updateTask(task);
//                runOnUiThread(() -> {
//                    updateCounters();
//                });
//            }
//        });
//    }
//
//    @Override
//    public void onTaskDeleted(Task task) {
//        // Delete task from database
//        Executors.newSingleThreadExecutor().execute(() -> {
//            database.taskDao().deleteTask(task);
//            runOnUiThread(() -> {
//                loadTasksFromDatabase(); // Reload to update the list
//                showSafeToast("Task deleted");
//            });
//        });
//    }
//
//    @Override
//    public void onTaskEdit(Task task) {
//        // Show edit dialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task_enhanced, null);
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
//            String newTitle = nameInput.getText().toString().trim();
//            if (newTitle.isEmpty()) {
//                nameInput.setError("Task name is required");
//                return;
//            }
//
//            task.setTitle(newTitle);
//            task.setDescription(descriptionInput.getText().toString().trim());
//
//            String hoursStr = hoursInput.getText().toString().trim();
//            String minutesStr = minutesInput.getText().toString().trim();
//            int newHours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
//            int newMinutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);
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
//            // Update in database
//            Executors.newSingleThreadExecutor().execute(() -> {
//                database.taskDao().updateTask(task);
//                runOnUiThread(() -> {
//                    adapter.notifyDataSetChanged();
//                    showSafeToast("Task updated");
//                });
//            });
//
//            dialog.dismiss();
//        });
//    }
//
//    @Override
//    public void onTaskReschedule(Task task) {
//        // Show date picker
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(task.getDueTimestamp());
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                (view, year, month, dayOfMonth) -> {
//                    // After date is selected, show time picker
//                    TimePickerDialog timePickerDialog = new TimePickerDialog(
//                            this,
//                            (timeView, hourOfDay, minute) -> {
//                                // Set the new due date
//                                Calendar newCalendar = Calendar.getInstance();
//                                newCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
//
//                                task.setDueTimestamp(newCalendar.getTimeInMillis());
//                                task.setScheduledDate(newCalendar.getTimeInMillis());
//
//                                // Update in database
//                                Executors.newSingleThreadExecutor().execute(() -> {
//                                    database.taskDao().updateTask(task);
//                                    runOnUiThread(() -> {
//                                        adapter.notifyDataSetChanged();
//                                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//                                        showSafeToast("Rescheduled to " + sdf.format(new Date(task.getDueTimestamp())));
//                                    });
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
//}