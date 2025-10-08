package com.tannazetm.dailytasktracker.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.tannazetm.dailytasktracker.Task;
import com.tannazetm.dailytasktracker.TaskRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final LiveData<List<Task>> todayTasks;
    private final LiveData<Integer> immediateCount;
    private final LiveData<Integer> completedCount;
    private final MutableLiveData<String> toastMessage;
    private final MutableLiveData<Task> taskToShowCompletion;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = TaskRepository.getInstance(application);
        todayTasks = repository.getTodayTasks();
        immediateCount = repository.getImmediateCount();
        completedCount = repository.getCompletedCount();
        toastMessage = new MutableLiveData<>();
        taskToShowCompletion = new MutableLiveData<>();

        // Initial data load
        refreshTasks();
    }

    public LiveData<List<Task>> getTodayTasks() {
        return todayTasks;
    }

    public LiveData<Integer> getImmediateCount() {
        return immediateCount;
    }

    public LiveData<Integer> getCompletedCount() {
        return completedCount;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public LiveData<Task> getTaskToShowCompletion() {
        return taskToShowCompletion;
    }

    public void refreshTasks() {
        repository.refreshTodayTasks();
    }

    public void insertTask(Task task) {
        repository.insertTask(task, insertedTask -> {
            toastMessage.postValue("Task created successfully!");
            // ISSUE #3 FIX: Force immediate refresh after insert
            refreshTasks();
        });
    }

    public void updateTask(Task task) {
        repository.updateTask(task);

        // ISSUE #3 FIX: Force immediate refresh after update
        refreshTasks();

        // Check if task was completed and has tracking data for completion dialog
        if (task.isCompleted() && task.getActualDuration() > 0) {
            taskToShowCompletion.postValue(task);
        }
    }

    public void deleteTask(Task task) {
        repository.deleteTask(task, () -> {
            toastMessage.postValue("Task deleted");
            // ISSUE #3 FIX: Force immediate refresh after delete
            refreshTasks();
        });
    }

    public void toggleTaskCompletion(Task task) {
        task.setCompleted(!task.isCompleted());
        if (task.isCompleted()) {
            if (task.isImmediate()) {
                task.setImmediate(false);
            }
            if (task.isInProgress()) {
                task.stopTask();
            }
        }
        updateTask(task);
    }

    public void toggleTaskImmediate(Task task) {
        repository.checkAndToggleImmediate(task, new TaskRepository.OnImmediateToggleCallback() {
            @Override
            public void onToggleSuccess() {
                // ISSUE #3 FIX: Force refresh to show updated immediate status
                refreshTasks();
            }

            @Override
            public void onToggleFailed(String message) {
                toastMessage.postValue(message);
                // Still refresh to ensure UI consistency
                refreshTasks();
            }
        });
    }

    public void toggleTaskTracking(Task task) {
        if (task.isInProgress()) {
            task.stopTask();
            toastMessage.postValue("Stopped: " + task.getFormattedActualDuration() + " tracked");
        } else {
            task.startTask();
            toastMessage.postValue("Started tracking");
        }
        updateTask(task);
    }

    public void rescheduleTask(Task task, long newTimestamp) {
        task.setDueTimestamp(newTimestamp);
        task.setScheduledDate(newTimestamp);
        updateTask(task);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        toastMessage.postValue("Rescheduled to " + sdf.format(new Date(newTimestamp)));
    }

    // Method to handle task edits with immediate update
    public void editTask(Task task) {
        repository.updateTask(task);
        toastMessage.postValue("Task updated successfully!");
        // ISSUE #3 FIX: Force refresh after edit
        refreshTasks();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanup();
    }
}





//package com.tannazetm.dailytasktracker.ui.home;
//
//import android.app.Application;
//import androidx.annotation.NonNull;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskRepository;
//import java.util.List;
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
//        });
//    }
//
//    public void updateTask(Task task) {
//        repository.updateTask(task);
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
//                // Success - UI will auto-update via LiveData
//            }
//
//            @Override
//            public void onToggleFailed(String message) {
//                toastMessage.postValue(message);
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
//        toastMessage.postValue("Task rescheduled");
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        repository.cleanup();
//    }
//}
//
