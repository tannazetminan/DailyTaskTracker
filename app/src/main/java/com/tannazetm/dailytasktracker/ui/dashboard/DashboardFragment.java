package com.tannazetm.dailytasktracker.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tannazetm.dailytasktracker.R;
import com.tannazetm.dailytasktracker.Task;
import com.tannazetm.dailytasktracker.TaskDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private TextView progressPercent;
    private TextView totalTasksCount;
    private TextView completedTasksCount;
    private TextView totalTimeSpent;
    private ProgressBar circularProgress;
    private TimeDistributionChart timeChart;
    private TaskDatabase database;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize database
        database = TaskDatabase.getDatabase(getContext());

        // Initialize views
        progressPercent = root.findViewById(R.id.progressPercent);
        totalTasksCount = root.findViewById(R.id.totalTasksCount);
        completedTasksCount = root.findViewById(R.id.completedTasksCount);
        totalTimeSpent = root.findViewById(R.id.totalTimeSpent);
        circularProgress = root.findViewById(R.id.circularProgress);
        timeChart = root.findViewById(R.id.timeChart);

        // Load and display statistics
        loadStatistics();

        return root;
    }

    private void loadStatistics() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Get all tasks from database
            List<Task> allTasks = database.taskDao().getAllTasks();

            // Calculate statistics
            int totalTasks = allTasks.size();
            int completedTasks = 0;
            int totalMinutesSpent = 0;

            // Time distribution by priority
            Map<Integer, Integer> priorityTimeMap = new HashMap<>();
            priorityTimeMap.put(1, 0); // Low
            priorityTimeMap.put(2, 0); // Medium
            priorityTimeMap.put(3, 0); // High

            for (Task task : allTasks) {
                if (task.isCompleted()) {
                    completedTasks++;
                }

                // Add actual duration if tracked, otherwise estimated for completed tasks
                int taskTime = 0;
                if (task.getActualDuration() > 0) {
                    taskTime = task.getActualDuration();
                    totalMinutesSpent += taskTime;
                } else if (task.isCompleted()) {
                    taskTime = task.getEstimatedDuration();
                    totalMinutesSpent += taskTime;
                }

                // Track time by priority
                if (taskTime > 0) {
                    int priority = task.getPriority();
                    priorityTimeMap.put(priority, priorityTimeMap.get(priority) + taskTime);
                }
            }

            // Calculate completion percentage
            int progressPercentage = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

            // Format time spent
            String timeSpentFormatted = formatDuration(totalMinutesSpent);

            // Prepare chart data
            int lowPriorityTime = priorityTimeMap.get(1);
            int mediumPriorityTime = priorityTimeMap.get(2);
            int highPriorityTime = priorityTimeMap.get(3);

            // Update UI on main thread
            int finalCompletedTasks = completedTasks;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Update all the views
                    progressPercent.setText(progressPercentage + "%");
                    totalTasksCount.setText(String.valueOf(totalTasks));
                    completedTasksCount.setText(String.valueOf(finalCompletedTasks));
                    totalTimeSpent.setText(timeSpentFormatted);
                    circularProgress.setProgress(progressPercentage);

                    // ISSUE #3 FIX: Update time distribution chart
                    if (timeChart != null) {
                        timeChart.setData(
                                lowPriorityTime,
                                mediumPriorityTime,
                                highPriorityTime
                        );
                    }
                });
            }
        });
    }

    private String formatDuration(int minutes) {
        if (minutes == 0) return "0m";
        if (minutes < 60) {
            return minutes + "m";
        } else {
            int hours = minutes / 60;
            int mins = minutes % 60;
            if (mins == 0) {
                return hours + "h";
            } else {
                return hours + "h " + mins + "m";
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh statistics when fragment becomes visible again
        loadStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}

//package com.tannazetm.dailytasktracker.ui.dashboard;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//
//import com.tannazetm.dailytasktracker.R;
//import com.tannazetm.dailytasktracker.Task;
//import com.tannazetm.dailytasktracker.TaskDatabase;
//
//import java.util.List;
//import java.util.concurrent.Executors;
//
//public class DashboardFragment extends Fragment {
//
//    private TextView progressPercent;
//    private TextView totalTasksCount;
//    private TextView completedTasksCount;
//    private TextView totalTimeSpent;
//    private ProgressBar circularProgress;
//    private TaskDatabase database;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//
//        // Initialize database
//        database = TaskDatabase.getDatabase(getContext());
//
//        // Initialize views
//        progressPercent = root.findViewById(R.id.progressPercent);
//        totalTasksCount = root.findViewById(R.id.totalTasksCount);
//        completedTasksCount = root.findViewById(R.id.completedTasksCount);
//        totalTimeSpent = root.findViewById(R.id.totalTimeSpent);
//        circularProgress = root.findViewById(R.id.circularProgress);
//
//        // Load and display statistics
//        loadStatistics();
//
//        return root;
//    }
//
//    private void loadStatistics() {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            // Get all tasks from database
//            List<Task> allTasks = database.taskDao().getAllTasks();
//
//            // Calculate statistics
//            int totalTasks = allTasks.size();
//            int completedTasks = 0;
//            int totalMinutesSpent = 0;
//
//            for (Task task : allTasks) {
//                if (task.isCompleted()) {
//                    completedTasks++;
//                }
//                // Add actual duration if tracked, otherwise estimated
//                if (task.getActualDuration() > 0) {
//                    totalMinutesSpent += task.getActualDuration();
//                } else if (task.isCompleted()) {
//                    totalMinutesSpent += task.getEstimatedDuration();
//                }
//            }
//
//            // Calculate completion percentage
//            int progressPercentage = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;
//
//            // Format time spent
//            String timeSpentFormatted = formatDuration(totalMinutesSpent);
//
//            // Update UI on main thread
//            int finalCompletedTasks = completedTasks;
//            getActivity().runOnUiThread(() -> {
//                // Update all the views
//                progressPercent.setText(progressPercentage + "%");
//                totalTasksCount.setText(String.valueOf(totalTasks));
//                completedTasksCount.setText(String.valueOf(finalCompletedTasks));
//                totalTimeSpent.setText(timeSpentFormatted);
//                circularProgress.setProgress(progressPercentage);
//            });
//        });
//    }
//
//    private String formatDuration(int minutes) {
//        if (minutes == 0) return "0m";
//        if (minutes < 60) {
//            return minutes + "m";
//        } else {
//            int hours = minutes / 60;
//            int mins = minutes % 60;
//            if (mins == 0) {
//                return hours + "h";
//            } else {
//                return hours + "h " + mins + "m";
//            }
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Refresh statistics when fragment becomes visible again
//        loadStatistics();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//    }
//}