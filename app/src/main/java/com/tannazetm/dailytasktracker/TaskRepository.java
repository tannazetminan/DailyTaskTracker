package com.tannazetm.dailytasktracker;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private static TaskRepository instance;
    private final TaskDao taskDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Task>> todayTasksLiveData;
    private final MutableLiveData<Integer> immediateCountLiveData;
    private final MutableLiveData<Integer> completedCountLiveData;

    // Cache to avoid repeated database calls
    private List<Task> cachedTodayTasks;
    private long lastCacheTime = 0;
    private static final long CACHE_VALIDITY = 5000; // 5 seconds cache

    private TaskRepository(Context context) {
        TaskDatabase database = TaskDatabase.getDatabase(context);
        taskDao = database.taskDao();
        executorService = Executors.newFixedThreadPool(4); // Thread pool for better performance
        todayTasksLiveData = new MutableLiveData<>();
        immediateCountLiveData = new MutableLiveData<>();
        completedCountLiveData = new MutableLiveData<>();
    }

    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Get today's tasks with caching
    public LiveData<List<Task>> getTodayTasks() {
        long currentTime = System.currentTimeMillis();
        if (cachedTodayTasks == null || (currentTime - lastCacheTime) > CACHE_VALIDITY) {
            refreshTodayTasks();
        }
        return todayTasksLiveData;
    }

    public void refreshTodayTasks() {
        executorService.execute(() -> {
            cachedTodayTasks = taskDao.getTodayTasks();
            lastCacheTime = System.currentTimeMillis();
            todayTasksLiveData.postValue(cachedTodayTasks);
            updateCounters();
        });
    }

    // Get counters as LiveData for automatic UI updates
    public LiveData<Integer> getImmediateCount() {
        return immediateCountLiveData;
    }

    public LiveData<Integer> getCompletedCount() {
        return completedCountLiveData;
    }

    private void updateCounters() {
        executorService.execute(() -> {
            int immediateCount = taskDao.getImmediateTaskCount();
            int completedCount = taskDao.getTodayCompletedCount();
            immediateCountLiveData.postValue(immediateCount);
            completedCountLiveData.postValue(completedCount);
        });
    }

    // Insert task with callback
    public void insertTask(Task task, OnTaskInsertedCallback callback) {
        executorService.execute(() -> {
            long taskId = taskDao.insertTask(task);
            task.setId((int) taskId);
            refreshTodayTasks(); // This will update LiveData
            if (callback != null) {
                callback.onTaskInserted(task);
            }
        });
    }

    // Update task
    public void updateTask(Task task) {
        executorService.execute(() -> {
            taskDao.updateTask(task);
            refreshTodayTasks();
        });
    }

    // Delete task
    public void deleteTask(Task task, OnTaskDeletedCallback callback) {
        executorService.execute(() -> {
            taskDao.deleteTask(task);
            refreshTodayTasks();
            if (callback != null) {
                callback.onTaskDeleted();
            }
        });
    }

    // Toggle immediate with validation
    public void toggleImmediate(Task task, OnImmediateToggleCallback callback) {
        executorService.execute(() -> {
            int currentCount = taskDao.getImmediateTaskCount();
            boolean canToggle = !task.isImmediate() || currentCount < 5;

            if (canToggle) {
                task.setImmediate(!task.isImmediate());
                taskDao.updateTask(task);
                refreshTodayTasks();
                callback.onToggleSuccess();
            } else {
                callback.onToggleFailed("Maximum 5 immediate tasks allowed");
            }
        });
    }

    // Check immediate count before toggling
    public void checkAndToggleImmediate(Task task, OnImmediateToggleCallback callback) {
        executorService.execute(() -> {
            int currentCount = taskDao.getImmediateTaskCount();
            if (task.isImmediate() || currentCount < 5) {
                taskDao.updateTask(task);
                refreshTodayTasks();
                callback.onToggleSuccess();
            } else {
                callback.onToggleFailed("Maximum 5 immediate tasks allowed");
            }
        });
    }

    // Callbacks for async operations
    public interface OnTaskInsertedCallback {
        void onTaskInserted(Task task);
    }

    public interface OnTaskDeletedCallback {
        void onTaskDeleted();
    }

    public interface OnImmediateToggleCallback {
        void onToggleSuccess();
        void onToggleFailed(String message);
    }

    // Clean up resources
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}