//package com.tannazetm.dailytasktracker.ui.notifications;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.tannazetm.dailytasktracker.databinding.FragmentNotificationsBinding;
//
//public class NotificationsFragment extends Fragment {
//
//    private FragmentNotificationsBinding binding;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        NotificationsViewModel notificationsViewModel =
//                new ViewModelProvider(this).get(NotificationsViewModel.class);
//
//        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();
//
//        final TextView textView = binding.textNotifications;
//        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
//        return root;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}


package com.tannazetm.dailytasktracker.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.tannazetm.dailytasktracker.R;
import com.tannazetm.dailytasktracker.Task;
import com.tannazetm.dailytasktracker.TaskDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class NotificationsFragment extends Fragment {

    private TaskDatabase database;

    // Views
    private CardView overdueCard, dueTodayCard, dueTomorrowCard;
    private TextView overdueCount, overdueTasksList;
    private TextView dueTodayCount, dueTodayTasksList;
    private TextView dueTomorrowCount, dueTomorrowTasksList;
    private TextView recentActivityText;
    private TextView totalActiveCount, totalCompletedCount, completionRate;
    private LinearLayout emptyStateLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize database
        database = TaskDatabase.getDatabase(getContext());

        // Initialize views
        initializeViews(root);

        // Load data
        loadNotifications();

        return root;
    }

    private void initializeViews(View root) {
        overdueCard = root.findViewById(R.id.overdueCard);
        overdueCount = root.findViewById(R.id.overdueCount);
        overdueTasksList = root.findViewById(R.id.overdueTasksList);

        dueTodayCard = root.findViewById(R.id.dueTodayCard);
        dueTodayCount = root.findViewById(R.id.dueTodayCount);
        dueTodayTasksList = root.findViewById(R.id.dueTodayTasksList);

        dueTomorrowCard = root.findViewById(R.id.dueTomorrowCard);
        dueTomorrowCount = root.findViewById(R.id.dueTomorrowCount);
        dueTomorrowTasksList = root.findViewById(R.id.dueTomorrowTasksList);

        recentActivityText = root.findViewById(R.id.recentActivityText);
        totalActiveCount = root.findViewById(R.id.totalActiveCount);
        totalCompletedCount = root.findViewById(R.id.totalCompletedCount);
        completionRate = root.findViewById(R.id.completionRate);
        emptyStateLayout = root.findViewById(R.id.emptyStateLayout);
    }

    private void loadNotifications() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Get current time
            long currentTime = System.currentTimeMillis();

            // Get overdue tasks
            List<Task> overdueTasks = database.taskDao().getOverdueTasksAll(currentTime);

            // Get today's tasks
            List<Task> todayTasks = database.taskDao().getTodayTasks();

            // Get tomorrow's tasks
            List<Task> tomorrowTasks = database.taskDao().getTomorrowTasks();

            // Get all tasks for stats
            List<Task> allTasks = database.taskDao().getAllTasks();

            // Calculate stats
            int activeTasksCount = 0;
            int completedTasksCount = 0;
            for (Task task : allTasks) {
                if (task.isCompleted()) {
                    completedTasksCount++;
                } else {
                    activeTasksCount++;
                }
            }

            int totalTasks = allTasks.size();
            int completionPercentage = totalTasks > 0 ? (completedTasksCount * 100) / totalTasks : 0;

            // Get recent activity (last 5 completed tasks)
            List<Task> completedTasks = database.taskDao().getCompletedTasks();
            StringBuilder recentActivity = new StringBuilder();
            int activityCount = Math.min(5, completedTasks.size());

            if (activityCount > 0) {
                for (int i = 0; i < activityCount; i++) {
                    Task task = completedTasks.get(i);
                    recentActivity.append("✓ ").append(task.getTitle());
                    if (i < activityCount - 1) {
                        recentActivity.append("\n");
                    }
                }
            } else {
                recentActivity.append("No recent activity");
            }

            // Build task lists
            String overdueListText = buildTaskList(overdueTasks);
            String todayListText = buildTaskList(todayTasks);
            String tomorrowListText = buildTaskList(tomorrowTasks);

            // Update UI on main thread
            if (getActivity() != null) {
                int finalActiveTasksCount = activeTasksCount;
                int finalCompletedTasksCount = completedTasksCount;
                getActivity().runOnUiThread(() -> {
                    // Update overdue card
                    if (overdueTasks.size() > 0) {
                        overdueCard.setVisibility(View.VISIBLE);
                        overdueCount.setText(String.valueOf(overdueTasks.size()));
                        overdueTasksList.setText(overdueListText);
                    } else {
                        overdueCard.setVisibility(View.GONE);
                    }

                    // Update due today card
                    if (todayTasks.size() > 0) {
                        dueTodayCard.setVisibility(View.VISIBLE);
                        dueTodayCount.setText(String.valueOf(todayTasks.size()));
                        dueTodayTasksList.setText(todayListText);
                    } else {
                        dueTodayCard.setVisibility(View.GONE);
                    }

                    // Update due tomorrow card
                    if (tomorrowTasks.size() > 0) {
                        dueTomorrowCard.setVisibility(View.VISIBLE);
                        dueTomorrowCount.setText(String.valueOf(tomorrowTasks.size()));
                        dueTomorrowTasksList.setText(tomorrowListText);
                    } else {
                        dueTomorrowCard.setVisibility(View.GONE);
                    }

                    // Update recent activity
                    recentActivityText.setText(recentActivity.toString());

                    // Update stats
                    totalActiveCount.setText(String.valueOf(finalActiveTasksCount));
                    totalCompletedCount.setText(String.valueOf(finalCompletedTasksCount));
                    completionRate.setText(completionPercentage + "%");

                    // Show/hide empty state
                    if (overdueTasks.size() == 0 && todayTasks.size() == 0 && tomorrowTasks.size() == 0) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private String buildTaskList(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return "No tasks";
        }

        StringBuilder builder = new StringBuilder();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Show max 5 tasks
        int count = Math.min(5, tasks.size());
        for (int i = 0; i < count; i++) {
            Task task = tasks.get(i);
            String timeStr = timeFormat.format(task.getDueTimestamp());

            builder.append("• ").append(task.getTitle());
            builder.append(" (").append(timeStr).append(")");

            if (i < count - 1) {
                builder.append("\n");
            }
        }

        if (tasks.size() > 5) {
            builder.append("\n... and ").append(tasks.size() - 5).append(" more");
        }

        return builder.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh notifications when fragment becomes visible
        loadNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}