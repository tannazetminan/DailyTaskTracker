package com.tannazetm.dailytasktracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks = new ArrayList<>();
    private Context context;
    private OnTaskInteractionListener listener;
    private Handler uiHandler;

    public interface OnTaskInteractionListener {
        void onTaskCompleted(Task task);
        void onImmediateToggled(Task task);
        void onTaskDeleted(Task task);
        void onTaskEdit(Task task);
        void onTaskReschedule(Task task);
    }

    public TaskAdapter(Context context, OnTaskInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.uiHandler = new Handler(Looper.getMainLooper());
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getId();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_enhanced, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> newTasks) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TaskDiffCallback(this.tasks, newTasks));
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    public Task getTaskAt(int position) {
        if (position >= 0 && position < tasks.size()) {
            return tasks.get(position);
        }
        return null;
    }

    private static class TaskDiffCallback extends DiffUtil.Callback {
        private List<Task> oldList;
        private List<Task> newList;

        TaskDiffCallback(List<Task> oldList, List<Task> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Task oldTask = oldList.get(oldItemPosition);
            Task newTask = newList.get(newItemPosition);
            return oldTask.isCompleted() == newTask.isCompleted() &&
                    oldTask.isImmediate() == newTask.isImmediate() &&
                    oldTask.isInProgress() == newTask.isInProgress() &&
                    oldTask.getTitle().equals(newTask.getTitle()) &&
                    oldTask.getActualDuration() == newTask.getActualDuration() &&
                    oldTask.getPriority() == newTask.getPriority() &&
                    oldTask.getEstimatedDuration() == newTask.getEstimatedDuration();
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, durationText, dueTimeText, trackingTimeText;
        ImageButton playPauseButton, immediateButton, moreButton;
        MaterialCheckBox taskCheckbox;
        ImageView priorityIndicator;
        View colorIndicator;
        ProgressBar progressBar;
        LinearLayout trackingInfoLayout;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.taskTitle);
            descriptionText = itemView.findViewById(R.id.taskDescription);
            durationText = itemView.findViewById(R.id.taskDuration);
            dueTimeText = itemView.findViewById(R.id.taskTimeStatus);
            trackingTimeText = itemView.findViewById(R.id.trackingTimeText);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
            immediateButton = itemView.findViewById(R.id.immediateButton);
            moreButton = itemView.findViewById(R.id.moreButton);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            progressBar = itemView.findViewById(R.id.taskProgress);
            trackingInfoLayout = itemView.findViewById(R.id.trackingInfoLayout);
        }

        void bind(Task task) {
            // Reset listeners to avoid issues
            taskCheckbox.setOnCheckedChangeListener(null);

            // Set task data
            titleText.setText(task.getTitle());
            taskCheckbox.setChecked(task.isCompleted());

            // Description visibility
            if (descriptionText != null) {
                if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                    descriptionText.setVisibility(View.VISIBLE);
                    descriptionText.setText(task.getDescription());
                } else {
                    descriptionText.setVisibility(View.GONE);
                }
            }

            // ISSUE #1 FIX: Show estimated vs actual time comparison
            String durationDisplay = formatDurationShort(task.getEstimatedDuration());
            int totalTracked = task.getTotalTrackedMinutes();

            if (totalTracked > 0) {
                int difference = totalTracked - task.getEstimatedDuration();
                if (difference > 0) {
                    // Over time
                    durationDisplay += " (+" + difference + "m)";
                    durationText.setTextColor(ContextCompat.getColor(context, R.color.overdue));
                } else if (difference < 0) {
                    // Under time
                    durationDisplay += " (" + difference + "m)";
                    durationText.setTextColor(ContextCompat.getColor(context, R.color.completed));
                } else {
                    // On time
                    durationDisplay += " (✓)";
                    durationText.setTextColor(ContextCompat.getColor(context, R.color.accent));
                }
            } else {
                durationText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
            durationText.setText(durationDisplay);

            dueTimeText.setText(formatDueTime(task.getDueTimestamp()));

            // Update color indicator
            if (task.isCompleted()) {
                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.completed));
            } else if (task.isOverdue()) {
                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.overdue));
                dueTimeText.setTextColor(ContextCompat.getColor(context, R.color.overdue));
            } else if (task.isImmediate()) {
                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.immediate_task));
            } else {
                colorIndicator.setBackgroundColor(Color.parseColor(task.getColorTag()));
                dueTimeText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }

            // ISSUE #2 FIX: Show all priority levels
            if (priorityIndicator != null) {
                priorityIndicator.setVisibility(View.VISIBLE);
                if (task.getPriority() == 3) {
                    priorityIndicator.setImageResource(R.drawable.ic_priority_high);
                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.overdue));
                } else if (task.getPriority() == 2) {
                    priorityIndicator.setImageResource(R.drawable.ic_priority_medium);
                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.accent));
                } else {
                    priorityIndicator.setImageResource(R.drawable.ic_priority_low);
                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.completed));
                }
            }

            // Tracking info - Enhanced for Issue #1
            if (task.isInProgress()) {
                if (trackingInfoLayout != null) {
                    trackingInfoLayout.setVisibility(View.VISIBLE);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    int progress = task.getProgress();
                    progressBar.setProgress(Math.min(progress, 100));

                    // Change progress bar color based on time status
                    if (progress > 100) {
                        progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.overdue));
                    } else if (progress > 80) {
                        progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.accent));
                    } else {
                        progressBar.setProgressTintList(ContextCompat.getColorStateList(context, R.color.completed));
                    }
                }
                if (trackingTimeText != null) {
                    int currentTotal = task.getTotalTrackedMinutes();
                    String trackingText = "Tracking: " + currentTotal + "m / " +
                            formatDurationShort(task.getEstimatedDuration());

                    // Add status indicator
                    if (currentTotal > task.getEstimatedDuration()) {
                        trackingText += " (Over)";
                        trackingTimeText.setTextColor(ContextCompat.getColor(context, R.color.overdue));
                    } else {
                        trackingTimeText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    }
                    trackingTimeText.setText(trackingText);
                }
                playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                if (trackingInfoLayout != null) {
                    trackingInfoLayout.setVisibility(View.GONE);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                playPauseButton.setImageResource(R.drawable.ic_play);
            }

            // Completion styling
            if (task.isCompleted()) {
                titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleText.setAlpha(0.6f);
                if (descriptionText != null) {
                    descriptionText.setAlpha(0.6f);
                }
                playPauseButton.setEnabled(false);
                playPauseButton.setAlpha(0.5f);
            } else {
                titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                titleText.setAlpha(1f);
                if (descriptionText != null) {
                    descriptionText.setAlpha(1f);
                }
                playPauseButton.setEnabled(true);
                playPauseButton.setAlpha(1f);
            }

            // Immediate status
            if (task.isImmediate()) {
                immediateButton.setImageResource(R.drawable.ic_star_filled);
                immediateButton.setColorFilter(ContextCompat.getColor(context, R.color.accent));
            } else {
                immediateButton.setImageResource(R.drawable.ic_star_outline);
                immediateButton.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
            }

            // Set listeners
            taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                if (isChecked) {
                    if (task.isImmediate()) {
                        task.setImmediate(false);
                    }
                    if (task.isInProgress()) {
                        task.stopTask();
                    }
                }
                listener.onTaskCompleted(task);
            });

            playPauseButton.setOnClickListener(v -> {
                if (task.isInProgress()) {
                    task.stopTask();
                    showToast("Stopped: " + task.getFormattedActualDuration() + " tracked");
                } else {
                    task.startTask();
                    showToast("Started tracking");
                }
                listener.onTaskCompleted(task);
            });

            immediateButton.setOnClickListener(v -> {
                task.setImmediate(!task.isImmediate());
                listener.onImmediateToggled(task);
            });

            moreButton.setOnClickListener(v -> showBottomSheetOptions(task));

            itemView.setOnLongClickListener(v -> {
                task.setCompleted(!task.isCompleted());
                if (task.isCompleted()) {
                    if (task.isImmediate()) {
                        task.setImmediate(false);
                    }
                    if (task.isInProgress()) {
                        task.stopTask();
                    }
                }
                listener.onTaskCompleted(task);
                return true;
            });
        }

        private String formatDurationShort(int minutes) {
            if (minutes < 60) {
                return minutes + "m";
            } else {
                int hours = minutes / 60;
                int mins = minutes % 60;
                return mins == 0 ? hours + "h" : hours + "h " + mins + "m";
            }
        }

        private String formatDueTime(long timestamp) {
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTimeInMillis(timestamp);

            Calendar today = Calendar.getInstance();
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);

            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

            if (isSameDay(taskCal, today)) {
                return "Today " + timeFormat.format(taskCal.getTime());
            } else if (isSameDay(taskCal, tomorrow)) {
                return "Tomorrow " + timeFormat.format(taskCal.getTime());
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
                return dateFormat.format(taskCal.getTime());
            }
        }

        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }

        private void showToast(String message) {
            uiHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }

        private void showBottomSheetOptions(Task task) {
            BottomSheetDialog bottomSheet = new BottomSheetDialog(context);
            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_task_options, null);

            TextView taskTitleSheet = sheetView.findViewById(R.id.taskTitleSheet);
            taskTitleSheet.setText(task.getTitle());

            View optionComplete = sheetView.findViewById(R.id.optionComplete);
            LinearLayout completeLayout = (LinearLayout) optionComplete;
            TextView completeText = (TextView) completeLayout.getChildAt(1);

            if (task.isCompleted()) {
                completeText.setText("Mark as Incomplete");
            } else {
                completeText.setText("Mark as Complete");
            }

            optionComplete.setOnClickListener(v -> {
                task.setCompleted(!task.isCompleted());
                if (task.isCompleted()) {
                    if (task.isImmediate()) {
                        task.setImmediate(false);
                    }
                    if (task.isInProgress()) {
                        task.stopTask();
                    }
                }
                listener.onTaskCompleted(task);
                bottomSheet.dismiss();
                showToast(task.isCompleted() ? "Task completed!" : "Task marked incomplete");
            });

            sheetView.findViewById(R.id.optionReschedule).setOnClickListener(v -> {
                listener.onTaskReschedule(task);
                bottomSheet.dismiss();
            });

            sheetView.findViewById(R.id.optionEdit).setOnClickListener(v -> {
                listener.onTaskEdit(task);
                bottomSheet.dismiss();
            });

            sheetView.findViewById(R.id.optionDelete).setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            listener.onTaskDeleted(task);
                            bottomSheet.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            bottomSheet.setContentView(sheetView);
            bottomSheet.show();
        }
    }
}





//package com.tannazetm.dailytasktracker;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.DiffUtil;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.bottomsheet.BottomSheetDialog;
//import com.google.android.material.checkbox.MaterialCheckBox;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//
//public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
//    private List<Task> tasks = new ArrayList<>();
//    private Context context;
//    private OnTaskInteractionListener listener;
//    private Handler uiHandler;
//
//    public interface OnTaskInteractionListener {
//        void onTaskCompleted(Task task);
//        void onImmediateToggled(Task task);
//        void onTaskDeleted(Task task);
//        void onTaskEdit(Task task);
//        void onTaskReschedule(Task task);
//    }
//
//    public TaskAdapter(Context context, OnTaskInteractionListener listener) {
//        this.context = context;
//        this.listener = listener;
//        this.uiHandler = new Handler(Looper.getMainLooper());
//        setHasStableIds(true); // Improve performance with stable IDs
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return tasks.get(position).getId();
//    }
//
//    @NonNull
//    @Override
//    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_task_enhanced, parent, false);
//        return new TaskViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
//        holder.bind(tasks.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return tasks.size();
//    }
//
//    // Use DiffUtil for efficient updates
//    public void setTasks(List<Task> newTasks) {
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TaskDiffCallback(this.tasks, newTasks));
//        this.tasks.clear();
//        this.tasks.addAll(newTasks);
//        diffResult.dispatchUpdatesTo(this);
//    }
//
//    public Task getTaskAt(int position) {
//        if (position >= 0 && position < tasks.size()) {
//            return tasks.get(position);
//        }
//        return null;
//    }
//
//    // DiffUtil callback for efficient list updates
//    private static class TaskDiffCallback extends DiffUtil.Callback {
//        private List<Task> oldList;
//        private List<Task> newList;
//
//        TaskDiffCallback(List<Task> oldList, List<Task> newList) {
//            this.oldList = oldList;
//            this.newList = newList;
//        }
//
//        @Override
//        public int getOldListSize() {
//            return oldList.size();
//        }
//
//        @Override
//        public int getNewListSize() {
//            return newList.size();
//        }
//
//        @Override
//        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
//            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
//        }
//
//        @Override
//        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
//            Task oldTask = oldList.get(oldItemPosition);
//            Task newTask = newList.get(newItemPosition);
//            return oldTask.isCompleted() == newTask.isCompleted() &&
//                    oldTask.isImmediate() == newTask.isImmediate() &&
//                    oldTask.isInProgress() == newTask.isInProgress() &&
//                    oldTask.getTitle().equals(newTask.getTitle()) &&
//                    oldTask.getActualDuration() == newTask.getActualDuration();
//        }
//    }
//
//    class TaskViewHolder extends RecyclerView.ViewHolder {
//        TextView titleText, descriptionText, durationText, dueTimeText, trackingTimeText;
//        ImageButton playPauseButton, immediateButton, moreButton;
//        MaterialCheckBox taskCheckbox;
//        ImageView priorityIndicator;
//        View colorIndicator;
//        ProgressBar progressBar;
//        LinearLayout trackingInfoLayout;
//
//        TaskViewHolder(@NonNull View itemView) {
//            super(itemView);
//            titleText = itemView.findViewById(R.id.taskTitle);
//            descriptionText = itemView.findViewById(R.id.taskDescription);
//            durationText = itemView.findViewById(R.id.taskDuration);
//            dueTimeText = itemView.findViewById(R.id.taskTimeStatus);
//            trackingTimeText = itemView.findViewById(R.id.trackingTimeText);
//            playPauseButton = itemView.findViewById(R.id.playPauseButton);
//            immediateButton = itemView.findViewById(R.id.immediateButton);
//            moreButton = itemView.findViewById(R.id.moreButton);
//            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
//            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
//            colorIndicator = itemView.findViewById(R.id.colorIndicator);
//            progressBar = itemView.findViewById(R.id.taskProgress);
//            trackingInfoLayout = itemView.findViewById(R.id.trackingInfoLayout);
//        }
//
//        void bind(Task task) {
//            // Reset listeners to avoid issues
//            taskCheckbox.setOnCheckedChangeListener(null);
//
//            // Set task data
//            titleText.setText(task.getTitle());
//            taskCheckbox.setChecked(task.isCompleted());
//
//            // Description visibility
//            if (descriptionText != null) {
//                if (task.getDescription() != null && !task.getDescription().isEmpty()) {
//                    descriptionText.setVisibility(View.VISIBLE);
//                    descriptionText.setText(task.getDescription());
//                } else {
//                    descriptionText.setVisibility(View.GONE);
//                }
//            }
//
//            durationText.setText(formatDurationShort(task.getEstimatedDuration()));
//            dueTimeText.setText(formatDueTime(task.getDueTimestamp()));
//
//            // Update color indicator
//            if (task.isCompleted()) {
//                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.completed));
//            } else if (task.isOverdue()) {
//                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.overdue));
//                dueTimeText.setTextColor(ContextCompat.getColor(context, R.color.overdue));
//            } else if (task.isImmediate()) {
//                colorIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.immediate_task));
//            } else {
//                colorIndicator.setBackgroundColor(Color.parseColor(task.getColorTag()));
//                dueTimeText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
//            }
//
//            // Priority indicator
//            if (priorityIndicator != null) {
//                if (task.getPriority() == 3) {
//                    priorityIndicator.setVisibility(View.VISIBLE);
//                    priorityIndicator.setImageResource(R.drawable.ic_priority_high);
//                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.overdue));
//                } else {
//                    priorityIndicator.setVisibility(View.GONE);
//                }
//            }
//
//            // Tracking info
//            if (task.isInProgress()) {
//                if (trackingInfoLayout != null) {
//                    trackingInfoLayout.setVisibility(View.VISIBLE);
//                }
//                if (progressBar != null) {
//                    progressBar.setVisibility(View.VISIBLE);
//                    progressBar.setProgress(task.getProgress());
//                }
//                if (trackingTimeText != null) {
//                    trackingTimeText.setText("Tracking: " + task.getTotalTrackedMinutes() + "m / " +
//                            formatDurationShort(task.getEstimatedDuration()));
//                }
//                playPauseButton.setImageResource(R.drawable.ic_pause);
//            } else {
//                if (trackingInfoLayout != null) {
//                    trackingInfoLayout.setVisibility(View.GONE);
//                }
//                if (progressBar != null) {
//                    progressBar.setVisibility(View.GONE);
//                }
//                playPauseButton.setImageResource(R.drawable.ic_play);
//            }
//
//            // Completion styling
//            if (task.isCompleted()) {
//                titleText.setPaintFlags(titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                titleText.setAlpha(0.6f);
//                if (descriptionText != null) {
//                    descriptionText.setAlpha(0.6f);
//                }
//                playPauseButton.setEnabled(false);
//                playPauseButton.setAlpha(0.5f);
//            } else {
//                titleText.setPaintFlags(titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                titleText.setAlpha(1f);
//                if (descriptionText != null) {
//                    descriptionText.setAlpha(1f);
//                }
//                playPauseButton.setEnabled(true);
//                playPauseButton.setAlpha(1f);
//            }
//
//            // Immediate status
//            if (task.isImmediate()) {
//                immediateButton.setImageResource(R.drawable.ic_star_filled);
//                immediateButton.setColorFilter(ContextCompat.getColor(context, R.color.accent));
//            } else {
//                immediateButton.setImageResource(R.drawable.ic_star_outline);
//                immediateButton.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
//            }
//
//            // Set listeners
//            taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                task.setCompleted(isChecked);
//                if (isChecked) {
//                    if (task.isImmediate()) {
//                        task.setImmediate(false);
//                    }
//                    if (task.isInProgress()) {
//                        task.stopTask();
//                    }
//                }
//                listener.onTaskCompleted(task);
//            });
//
//            playPauseButton.setOnClickListener(v -> {
//                if (task.isInProgress()) {
//                    task.stopTask();
//                    showToast("Stopped: " + task.getFormattedActualDuration() + " tracked");
//                } else {
//                    task.startTask();
//                    showToast("Started tracking");
//                }
//                listener.onTaskCompleted(task);
//            });
//
//            immediateButton.setOnClickListener(v -> {
//                listener.onImmediateToggled(task);
//            });
//
//            moreButton.setOnClickListener(v -> showBottomSheetOptions(task));
//
//            itemView.setOnLongClickListener(v -> {
//                task.setCompleted(!task.isCompleted());
//                if (task.isCompleted()) {
//                    if (task.isImmediate()) {
//                        task.setImmediate(false);
//                    }
//                    if (task.isInProgress()) {
//                        task.stopTask();
//                    }
//                }
//                listener.onTaskCompleted(task);
//                return true;
//            });
//        }
//
//        private String formatDurationShort(int minutes) {
//            if (minutes < 60) {
//                return minutes + "m";
//            } else {
//                int hours = minutes / 60;
//                int mins = minutes % 60;
//                return mins == 0 ? hours + "h" : hours + "h " + mins + "m";
//            }
//        }
//
//        private String formatDueTime(long timestamp) {
//            Calendar taskCal = Calendar.getInstance();
//            taskCal.setTimeInMillis(timestamp);
//
//            Calendar today = Calendar.getInstance();
//            Calendar tomorrow = Calendar.getInstance();
//            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
//
//            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
//
//            if (isSameDay(taskCal, today)) {
//                return "Today " + timeFormat.format(taskCal.getTime());
//            } else if (isSameDay(taskCal, tomorrow)) {
//                return "Tomorrow " + timeFormat.format(taskCal.getTime());
//            } else {
//                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//                return dateFormat.format(taskCal.getTime());
//            }
//        }
//
//        private boolean isSameDay(Calendar cal1, Calendar cal2) {
//            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//        }
//
//        private void showToast(String message) {
//            uiHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
//        }
//
//        private void showBottomSheetOptions(Task task) {
//            BottomSheetDialog bottomSheet = new BottomSheetDialog(context);
//            View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_task_options, null);
//
//            TextView taskTitleSheet = sheetView.findViewById(R.id.taskTitleSheet);
//            taskTitleSheet.setText(task.getTitle());
//
//            View optionComplete = sheetView.findViewById(R.id.optionComplete);
//            LinearLayout completeLayout = (LinearLayout) optionComplete;
//            TextView completeText = (TextView) completeLayout.getChildAt(1);
//
//            if (task.isCompleted()) {
//                completeText.setText("Mark as Incomplete");
//            } else {
//                completeText.setText("Mark as Complete");
//            }
//
//            optionComplete.setOnClickListener(v -> {
//                task.setCompleted(!task.isCompleted());
//                if (task.isCompleted()) {
//                    if (task.isImmediate()) {
//                        task.setImmediate(false);
//                    }
//                    if (task.isInProgress()) {
//                        task.stopTask();
//                    }
//                }
//                listener.onTaskCompleted(task);
//                bottomSheet.dismiss();
//                showToast(task.isCompleted() ? "Task completed!" : "Task marked incomplete");
//            });
//
//            sheetView.findViewById(R.id.optionReschedule).setOnClickListener(v -> {
//                listener.onTaskReschedule(task);
//                bottomSheet.dismiss();
//            });
//
//            sheetView.findViewById(R.id.optionEdit).setOnClickListener(v -> {
//                listener.onTaskEdit(task);
//                bottomSheet.dismiss();
//            });
//
//            sheetView.findViewById(R.id.optionDelete).setOnClickListener(v -> {
//                new androidx.appcompat.app.AlertDialog.Builder(context)
//                        .setTitle("Delete Task")
//                        .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
//                        .setPositiveButton("Delete", (dialog, which) -> {
//                            listener.onTaskDeleted(task);
//                            bottomSheet.dismiss();
//                        })
//                        .setNegativeButton("Cancel", null)
//                        .show();
//            });
//
//            bottomSheet.setContentView(sheetView);
//            bottomSheet.show();
//        }
//    }
//}
//
