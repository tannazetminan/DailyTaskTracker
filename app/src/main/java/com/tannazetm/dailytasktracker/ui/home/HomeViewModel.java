package com.tannazetm.dailytasktracker.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.tannazetm.dailytasktracker.Task;
import com.tannazetm.dailytasktracker.TaskDatabase;
import com.tannazetm.dailytasktracker.util.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeViewModel extends AndroidViewModel {

    private final TaskDatabase database;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Task>> allTasksLiveData;
    private final MutableLiveData<List<Task>> filteredTasksLiveData;
    private final MutableLiveData<Integer> immediateCountLiveData;
    private final MutableLiveData<Integer> completedCountLiveData;
    private final MutableLiveData<Event<String>> toastMessageLiveData;
    private final MutableLiveData<Event<Task>> taskToShowCompletionLiveData;

    // Filter state
    private String dateFilter = "today";
    private String statusFilter = "all";
    private int priorityFilter = -1; // -1 means all
    private long customDateStart = 0;
    private long customDateEnd = 0;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        database = TaskDatabase.getDatabase(application);
        executorService = Executors.newFixedThreadPool(4);

        allTasksLiveData = new MutableLiveData<>();
        filteredTasksLiveData = new MutableLiveData<>();
        immediateCountLiveData = new MutableLiveData<>();
        completedCountLiveData = new MutableLiveData<>();
        toastMessageLiveData = new MutableLiveData<>();
        taskToShowCompletionLiveData = new MutableLiveData<>();

        // Initial data load
        refreshTasks();
    }

    public LiveData<List<Task>> getFilteredTasks() {
        return filteredTasksLiveData;
    }

    public LiveData<Integer> getImmediateCount() {
        return immediateCountLiveData;
    }

    public LiveData<Integer> getCompletedCount() {
        return completedCountLiveData;
    }

    public LiveData<Event<String>> getToastMessage() {
        return toastMessageLiveData;
    }

    public LiveData<Event<Task>> getTaskToShowCompletion() {
        return taskToShowCompletionLiveData;
    }

    // Filter setters
    public void setDateFilter(String filter) {
        this.dateFilter = filter;
        refreshTasks();
    }

    public void setStatusFilter(String filter) {
        this.statusFilter = filter;
        refreshTasks();
    }

    public void setPriorityFilter(int priority) {
        this.priorityFilter = priority;
        refreshTasks();
    }

    public void setCustomDateFilter(long dateMillis) {
        this.dateFilter = "custom";

        // Set start of day
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(dateMillis);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        this.customDateStart = startCal.getTimeInMillis();

        // Set end of day
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(dateMillis);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        this.customDateEnd = endCal.getTimeInMillis();

        refreshTasks();
    }

    public void clearFilters() {
        this.dateFilter = "today";
        this.statusFilter = "all";
        this.priorityFilter = -1;
        refreshTasks();
    }

    public String getActiveFiltersSummary() {
        List<String> activeFilters = new ArrayList<>();

        // Date filter
        if (dateFilter.equals("today")) {
            activeFilters.add("Today");
        } else if (dateFilter.equals("tomorrow")) {
            activeFilters.add("Tomorrow");
        } else if (dateFilter.equals("week")) {
            activeFilters.add("This Week");
        } else if (dateFilter.equals("custom")) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            activeFilters.add(sdf.format(new Date(customDateStart)));
        } else if (!dateFilter.equals("all")) {
            activeFilters.add("All");
        }

        // Status filter
        if (!statusFilter.equals("all")) {
            activeFilters.add(statusFilter.substring(0, 1).toUpperCase() + statusFilter.substring(1));
        }

        // Priority filter
        if (priorityFilter != -1) {
            String priorityText = priorityFilter == 3 ? "High" : (priorityFilter == 2 ? "Medium" : "Low");
            activeFilters.add(priorityText + " Priority");
        }

        return String.join(" • ", activeFilters);
    }

    public void refreshTasks() {
        executorService.execute(() -> {
            List<Task> tasks = fetchTasksBasedOnFilters();
            filteredTasksLiveData.postValue(tasks);
            updateCounters();
        });
    }

    private List<Task> fetchTasksBasedOnFilters() {
        List<Task> tasks;

        // Determine date range
        long startDate = 0;
        long endDate = 0;
        boolean useDateRange = false;

        if (dateFilter.equals("today")) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startDate = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTimeInMillis();
            useDateRange = true;
        } else if (dateFilter.equals("tomorrow")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startDate = cal.getTimeInMillis();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTimeInMillis();
            useDateRange = true;
        } else if (dateFilter.equals("week")) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startDate = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_YEAR, 7);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTimeInMillis();
            useDateRange = true;
        } else if (dateFilter.equals("custom")) {
            startDate = customDateStart;
            endDate = customDateEnd;
            useDateRange = true;
        }

        // Fetch tasks based on combined filters
        if (statusFilter.equals("overdue")) {
            // Overdue tasks
            if (priorityFilter == -1) {
                tasks = database.taskDao().getOverdueTasksAll(System.currentTimeMillis());
            } else {
                tasks = database.taskDao().getOverdueTasksByPriority(System.currentTimeMillis(), priorityFilter);
            }
        } else if (statusFilter.equals("active")) {
            // Active tasks
            if (useDateRange) {
                if (priorityFilter == -1) {
                    tasks = database.taskDao().getActiveTasksByDate(startDate, endDate);
                } else {
                    tasks = database.taskDao().getActiveTasksByDateAndPriority(startDate, endDate, priorityFilter);
                }
            } else {
                if (priorityFilter == -1) {
                    tasks = database.taskDao().getActiveTasks();
                } else {
                    tasks = database.taskDao().getActiveTasksByPriority(priorityFilter);
                }
            }
        } else if (statusFilter.equals("completed")) {
            // Completed tasks
            if (useDateRange) {
                if (priorityFilter == -1) {
                    tasks = database.taskDao().getCompletedTasksByDate(startDate, endDate);
                } else {
                    tasks = database.taskDao().getCompletedTasksByDateAndPriority(startDate, endDate, priorityFilter);
                }
            } else {
                if (priorityFilter == -1) {
                    tasks = database.taskDao().getCompletedTasks();
                } else {
                    tasks = database.taskDao().getCompletedTasksByPriority(priorityFilter);
                }
            }
        } else {
            // All tasks
            if (useDateRange) {
                if (priorityFilter == -1) {
                    tasks = database.taskDao().getTasksByDateRange(startDate, endDate);
                } else {
                    tasks = database.taskDao().getTasksByDateAndPriority(startDate, endDate, priorityFilter);
                }
            } else {
                if (priorityFilter == -1) {
                    tasks = database.taskDao().getAllTasks();
                } else {
                    tasks = database.taskDao().getTasksByPriority(priorityFilter);
                }
            }
        }

        return tasks;
    }

    private void updateCounters() {
        executorService.execute(() -> {
            int immediateCount = database.taskDao().getImmediateTaskCount();
            int completedCount = database.taskDao().getTodayCompletedCount();
            immediateCountLiveData.postValue(immediateCount);
            completedCountLiveData.postValue(completedCount);
        });
    }

    public void insertTask(Task task) {
        executorService.execute(() -> {
            long taskId = database.taskDao().insertTask(task);
            task.setId((int) taskId);
            toastMessageLiveData.postValue(new Event<>("Task created successfully!"));
            refreshTasks();
        });
    }

    public void updateTask(Task task) {
        executorService.execute(() -> {
            database.taskDao().updateTask(task);
            refreshTasks();

            // Check if task was completed and has tracking data for completion dialog
            if (task.isCompleted() && task.getActualDuration() > 0) {
                taskToShowCompletionLiveData.postValue(new Event<>(task));
            }
        });
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> {
            database.taskDao().deleteTask(task);
            toastMessageLiveData.postValue(new Event<>("Task deleted"));
            refreshTasks();
        });
    }

    public void toggleTaskImmediate(Task task) {
        executorService.execute(() -> {
            int currentCount = database.taskDao().getImmediateTaskCount();
            if (task.isImmediate() || currentCount < 5) {
                database.taskDao().updateTask(task);
                refreshTasks();
            } else {
                toastMessageLiveData.postValue(new Event<>("Maximum 5 immediate tasks allowed"));
                refreshTasks();
            }
        });
    }

    public void rescheduleTask(Task task, long newTimestamp) {
        task.setDueTimestamp(newTimestamp);
        task.setScheduledDate(newTimestamp);
        updateTask(task);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        toastMessageLiveData.postValue(new Event<>("Rescheduled to " + sdf.format(new Date(newTimestamp))));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}




//package com.tannazetm.dailytasktracker.ui.home;
//
//import android.app.Application;
//import androidx.annotation.NonNull;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.Transformations;
//
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskDatabase;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class HomeViewModel extends AndroidViewModel {
//
//    private final TaskDatabase database;
//    private final ExecutorService executorService;
//
//    private final MutableLiveData<List<Task>> allTasksLiveData;
//    private final MutableLiveData<List<Task>> filteredTasksLiveData;
//    private final MutableLiveData<Integer> immediateCountLiveData;
//    private final MutableLiveData<Integer> completedCountLiveData;
//    private final MutableLiveData<String> toastMessageLiveData;
//    private final MutableLiveData<Task> taskToShowCompletionLiveData;
//
//    // Filter state
//    private String dateFilter = "today";
//    private String statusFilter = "all";
//    private int priorityFilter = -1; // -1 means all
//    private long customDateStart = 0;
//    private long customDateEnd = 0;
//
//    public HomeViewModel(@NonNull Application application) {
//        super(application);
//        database = TaskDatabase.getDatabase(application);
//        executorService = Executors.newFixedThreadPool(4);
//
//        allTasksLiveData = new MutableLiveData<>();
//        filteredTasksLiveData = new MutableLiveData<>();
//        immediateCountLiveData = new MutableLiveData<>();
//        completedCountLiveData = new MutableLiveData<>();
//        toastMessageLiveData = new MutableLiveData<>();
//        taskToShowCompletionLiveData = new MutableLiveData<>();
//
//        // Initial data load
//        refreshTasks();
//    }
//
//    public LiveData<List<Task>> getFilteredTasks() {
//        return filteredTasksLiveData;
//    }
//
//    public LiveData<Integer> getImmediateCount() {
//        return immediateCountLiveData;
//    }
//
//    public LiveData<Integer> getCompletedCount() {
//        return completedCountLiveData;
//    }
//
//    public LiveData<String> getToastMessage() {
//        return toastMessageLiveData;
//    }
//
//    public LiveData<Task> getTaskToShowCompletion() {
//        return taskToShowCompletionLiveData;
//    }
//
//    // Filter setters
//    public void setDateFilter(String filter) {
//        this.dateFilter = filter;
//        refreshTasks();
//    }
//
//    public void setStatusFilter(String filter) {
//        this.statusFilter = filter;
//        refreshTasks();
//    }
//
//    public void setPriorityFilter(int priority) {
//        this.priorityFilter = priority;
//        refreshTasks();
//    }
//
//    public void setCustomDateFilter(long dateMillis) {
//        this.dateFilter = "custom";
//
//        // Set start of day
//        Calendar startCal = Calendar.getInstance();
//        startCal.setTimeInMillis(dateMillis);
//        startCal.set(Calendar.HOUR_OF_DAY, 0);
//        startCal.set(Calendar.MINUTE, 0);
//        startCal.set(Calendar.SECOND, 0);
//        this.customDateStart = startCal.getTimeInMillis();
//
//        // Set end of day
//        Calendar endCal = Calendar.getInstance();
//        endCal.setTimeInMillis(dateMillis);
//        endCal.set(Calendar.HOUR_OF_DAY, 23);
//        endCal.set(Calendar.MINUTE, 59);
//        endCal.set(Calendar.SECOND, 59);
//        this.customDateEnd = endCal.getTimeInMillis();
//
//        refreshTasks();
//    }
//
//    public void clearFilters() {
//        this.dateFilter = "today";
//        this.statusFilter = "all";
//        this.priorityFilter = -1;
//        refreshTasks();
//    }
//
//    public String getActiveFiltersSummary() {
//        List<String> activeFilters = new ArrayList<>();
//
//        // Date filter
//        if (dateFilter.equals("today")) {
//            activeFilters.add("Today");
//        } else if (dateFilter.equals("tomorrow")) {
//            activeFilters.add("Tomorrow");
//        } else if (dateFilter.equals("week")) {
//            activeFilters.add("This Week");
//        } else if (dateFilter.equals("custom")) {
//            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
//            activeFilters.add(sdf.format(new Date(customDateStart)));
//        } else if (!dateFilter.equals("all")) {
//            activeFilters.add("All");
//        }
//
//        // Status filter
//        if (!statusFilter.equals("all")) {
//            activeFilters.add(statusFilter.substring(0, 1).toUpperCase() + statusFilter.substring(1));
//        }
//
//        // Priority filter
//        if (priorityFilter != -1) {
//            String priorityText = priorityFilter == 3 ? "High" : (priorityFilter == 2 ? "Medium" : "Low");
//            activeFilters.add(priorityText + " Priority");
//        }
//
//        return String.join(" • ", activeFilters);
//    }
//
//    public void refreshTasks() {
//        executorService.execute(() -> {
//            List<Task> tasks = fetchTasksBasedOnFilters();
//            filteredTasksLiveData.postValue(tasks);
//            updateCounters();
//        });
//    }
//
//    private List<Task> fetchTasksBasedOnFilters() {
//        List<Task> tasks;
//
//        // Determine date range
//        long startDate = 0;
//        long endDate = 0;
//        boolean useDateRange = false;
//
//        if (dateFilter.equals("today")) {
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.SECOND, 0);
//            startDate = cal.getTimeInMillis();
//
//            cal.set(Calendar.HOUR_OF_DAY, 23);
//            cal.set(Calendar.MINUTE, 59);
//            cal.set(Calendar.SECOND, 59);
//            endDate = cal.getTimeInMillis();
//            useDateRange = true;
//        } else if (dateFilter.equals("tomorrow")) {
//            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.DAY_OF_YEAR, 1);
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.SECOND, 0);
//            startDate = cal.getTimeInMillis();
//
//            cal.set(Calendar.HOUR_OF_DAY, 23);
//            cal.set(Calendar.MINUTE, 59);
//            cal.set(Calendar.SECOND, 59);
//            endDate = cal.getTimeInMillis();
//            useDateRange = true;
//        } else if (dateFilter.equals("week")) {
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, 0);
//            cal.set(Calendar.MINUTE, 0);
//            cal.set(Calendar.SECOND, 0);
//            startDate = cal.getTimeInMillis();
//
//            cal.add(Calendar.DAY_OF_YEAR, 7);
//            cal.set(Calendar.HOUR_OF_DAY, 23);
//            cal.set(Calendar.MINUTE, 59);
//            cal.set(Calendar.SECOND, 59);
//            endDate = cal.getTimeInMillis();
//            useDateRange = true;
//        } else if (dateFilter.equals("custom")) {
//            startDate = customDateStart;
//            endDate = customDateEnd;
//            useDateRange = true;
//        }
//
//        // Fetch tasks based on combined filters
//        if (statusFilter.equals("overdue")) {
//            // Overdue tasks
//            if (priorityFilter == -1) {
//                tasks = database.taskDao().getOverdueTasksAll(System.currentTimeMillis());
//            } else {
//                tasks = database.taskDao().getOverdueTasksByPriority(System.currentTimeMillis(), priorityFilter);
//            }
//        } else if (statusFilter.equals("active")) {
//            // Active tasks
//            if (useDateRange) {
//                if (priorityFilter == -1) {
//                    tasks = database.taskDao().getActiveTasksByDate(startDate, endDate);
//                } else {
//                    tasks = database.taskDao().getActiveTasksByDateAndPriority(startDate, endDate, priorityFilter);
//                }
//            } else {
//                if (priorityFilter == -1) {
//                    tasks = database.taskDao().getActiveTasks();
//                } else {
//                    tasks = database.taskDao().getActiveTasksByPriority(priorityFilter);
//                }
//            }
//        } else if (statusFilter.equals("completed")) {
//            // Completed tasks
//            if (useDateRange) {
//                if (priorityFilter == -1) {
//                    tasks = database.taskDao().getCompletedTasksByDate(startDate, endDate);
//                } else {
//                    tasks = database.taskDao().getCompletedTasksByDateAndPriority(startDate, endDate, priorityFilter);
//                }
//            } else {
//                if (priorityFilter == -1) {
//                    tasks = database.taskDao().getCompletedTasks();
//                } else {
//                    tasks = database.taskDao().getCompletedTasksByPriority(priorityFilter);
//                }
//            }
//        } else {
//            // All tasks
//            if (useDateRange) {
//                if (priorityFilter == -1) {
//                    tasks = database.taskDao().getTasksByDateRange(startDate, endDate);
//                } else {
//                    tasks = database.taskDao().getTasksByDateAndPriority(startDate, endDate, priorityFilter);
//                }
//            } else {
//                if (priorityFilter == -1) {
//                    tasks = database.taskDao().getAllTasks();
//                } else {
//                    tasks = database.taskDao().getTasksByPriority(priorityFilter);
//                }
//            }
//        }
//
//        return tasks;
//    }
//
//    private void updateCounters() {
//        executorService.execute(() -> {
//            int immediateCount = database.taskDao().getImmediateTaskCount();
//            int completedCount = database.taskDao().getTodayCompletedCount();
//            immediateCountLiveData.postValue(immediateCount);
//            completedCountLiveData.postValue(completedCount);
//        });
//    }
//
//    public void insertTask(Task task) {
//        executorService.execute(() -> {
//            long taskId = database.taskDao().insertTask(task);
//            task.setId((int) taskId);
//            toastMessageLiveData.postValue("Task created successfully!");
//            refreshTasks();
//        });
//    }
//
//    public void updateTask(Task task) {
//        executorService.execute(() -> {
//            database.taskDao().updateTask(task);
//            refreshTasks();
//
//            // Check if task was completed and has tracking data for completion dialog
//            if (task.isCompleted() && task.getActualDuration() > 0) {
//                taskToShowCompletionLiveData.postValue(task);
//            }
//        });
//    }
//
//    public void deleteTask(Task task) {
//        executorService.execute(() -> {
//            database.taskDao().deleteTask(task);
//            toastMessageLiveData.postValue("Task deleted");
//            refreshTasks();
//        });
//    }
//
//    public void toggleTaskImmediate(Task task) {
//        executorService.execute(() -> {
//            int currentCount = database.taskDao().getImmediateTaskCount();
//            if (task.isImmediate() || currentCount < 5) {
//                database.taskDao().updateTask(task);
//                refreshTasks();
//            } else {
//                toastMessageLiveData.postValue("Maximum 5 immediate tasks allowed");
//                refreshTasks();
//            }
//        });
//    }
//
//    public void rescheduleTask(Task task, long newTimestamp) {
//        task.setDueTimestamp(newTimestamp);
//        task.setScheduledDate(newTimestamp);
//        updateTask(task);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//        toastMessageLiveData.postValue("Rescheduled to " + sdf.format(new Date(newTimestamp)));
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        if (executorService != null && !executorService.isShutdown()) {
//            executorService.shutdown();
//        }
//    }
//}













//package com.tannazetm.dailytasktracker.ui.home;
//
//import android.app.Application;
//import androidx.annotation.NonNull;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskRepository;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class HomeViewModel extends AndroidViewModel {
//
//    private final TaskRepository repository;
//    private final LiveData<List<Task>> todayTasks;
//    private final LiveData<Integer> immediateCount;
//    private final LiveData<Integer> completedCount;
//    private final MutableLiveData<String> toastMessage;
//    private final MutableLiveData<Task> taskToShowCompletion;
//
//    public HomeViewModel(@NonNull Application application) {
//        super(application);
//        repository = TaskRepository.getInstance(application);
//        todayTasks = repository.getTodayTasks();
//        immediateCount = repository.getImmediateCount();
//        completedCount = repository.getCompletedCount();
//        toastMessage = new MutableLiveData<>();
//        taskToShowCompletion = new MutableLiveData<>();
//
//        // Initial data load
//        refreshTasks();
//    }
//
//    public LiveData<List<Task>> getTodayTasks() {
//        return todayTasks;
//    }
//
//    public LiveData<Integer> getImmediateCount() {
//        return immediateCount;
//    }
//
//    public LiveData<Integer> getCompletedCount() {
//        return completedCount;
//    }
//
//    public LiveData<String> getToastMessage() {
//        return toastMessage;
//    }
//
//    public LiveData<Task> getTaskToShowCompletion() {
//        return taskToShowCompletion;
//    }
//
//    public void refreshTasks() {
//        repository.refreshTodayTasks();
//    }
//
//    public void insertTask(Task task) {
//        repository.insertTask(task, insertedTask -> {
//            toastMessage.postValue("Task created successfully!");
//            // ISSUE #3 FIX: Force immediate refresh after insert
//            refreshTasks();
//        });
//    }
//
//    public void updateTask(Task task) {
//        repository.updateTask(task);
//
//        // ISSUE #3 FIX: Force immediate refresh after update
//        refreshTasks();
//
//        // Check if task was completed and has tracking data for completion dialog
//        if (task.isCompleted() && task.getActualDuration() > 0) {
//            taskToShowCompletion.postValue(task);
//        }
//    }
//
//    public void deleteTask(Task task) {
//        repository.deleteTask(task, () -> {
//            toastMessage.postValue("Task deleted");
//            // ISSUE #3 FIX: Force immediate refresh after delete
//            refreshTasks();
//        });
//    }
//
//    public void toggleTaskCompletion(Task task) {
//        task.setCompleted(!task.isCompleted());
//        if (task.isCompleted()) {
//            if (task.isImmediate()) {
//                task.setImmediate(false);
//            }
//            if (task.isInProgress()) {
//                task.stopTask();
//            }
//        }
//        updateTask(task);
//    }
//
//    public void toggleTaskImmediate(Task task) {
//        repository.checkAndToggleImmediate(task, new TaskRepository.OnImmediateToggleCallback() {
//            @Override
//            public void onToggleSuccess() {
//                // ISSUE #3 FIX: Force refresh to show updated immediate status
//                refreshTasks();
//            }
//
//            @Override
//            public void onToggleFailed(String message) {
//                toastMessage.postValue(message);
//                // Still refresh to ensure UI consistency
//                refreshTasks();
//            }
//        });
//    }
//
//    public void toggleTaskTracking(Task task) {
//        if (task.isInProgress()) {
//            task.stopTask();
//            toastMessage.postValue("Stopped: " + task.getFormattedActualDuration() + " tracked");
//        } else {
//            task.startTask();
//            toastMessage.postValue("Started tracking");
//        }
//        updateTask(task);
//    }
//
//    public void rescheduleTask(Task task, long newTimestamp) {
//        task.setDueTimestamp(newTimestamp);
//        task.setScheduledDate(newTimestamp);
//        updateTask(task);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//        toastMessage.postValue("Rescheduled to " + sdf.format(new Date(newTimestamp)));
//    }
//
//    // Method to handle task edits with immediate update
//    public void editTask(Task task) {
//        repository.updateTask(task);
//        toastMessage.postValue("Task updated successfully!");
//        // ISSUE #3 FIX: Force refresh after edit
//        refreshTasks();
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        repository.cleanup();
//    }
//}
//
//
//
