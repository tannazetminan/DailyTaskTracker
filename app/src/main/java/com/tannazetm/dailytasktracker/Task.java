package com.tannazetm.dailytasktracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private long createdTimestamp;
    private long dueTimestamp;
    private long scheduledDate;
    private long startTime; // When current tracking session started
    private long endTime;
    private int estimatedDuration; // Duration in minutes
    private int actualDuration; // Accumulated actual duration in minutes
    private boolean isCompleted;
    private boolean isImmediate;
    private boolean isInProgress;
    private int priority; // 1=Low, 2=Medium, 3=High
    private String category;
    private String colorTag;

    // Constructor for new tasks
    public Task(String title, String description, long dueTimestamp, int estimatedDuration) {
        this.title = title;
        this.description = description;
        this.createdTimestamp = System.currentTimeMillis();
        this.dueTimestamp = dueTimestamp;
        this.scheduledDate = dueTimestamp;
        this.estimatedDuration = estimatedDuration;
        this.actualDuration = 0;
        this.startTime = 0;
        this.endTime = 0;
        this.isCompleted = false;
        this.isImmediate = false;
        this.isInProgress = false;
        this.priority = 2;
        this.category = "General";
        this.colorTag = "#1E40AF"; // Professional blue
    }

    // Simple constructor
    public Task(String title) {
        this(title, "", System.currentTimeMillis() + (24 * 60 * 60 * 1000), 30);
    }

    // Empty constructor for Room
    public Task() {
        this.createdTimestamp = System.currentTimeMillis();
        this.estimatedDuration = 30;
        this.colorTag = "#1E40AF";
    }

    // Start tracking time (or resume)
    public void startTask() {
        this.startTime = System.currentTimeMillis();
        this.isInProgress = true;
    }

    // Stop tracking time and accumulate duration
    public void stopTask() {
        if (startTime > 0 && isInProgress) {
            this.endTime = System.currentTimeMillis();
            // Add the elapsed time to accumulated duration
            int sessionMinutes = (int) ((endTime - startTime) / 60000);
            this.actualDuration += sessionMinutes;
            this.isInProgress = false;
            this.startTime = 0; // Reset for next session
        }
    }

    // Get current session elapsed time (for live tracking display)
    public int getCurrentSessionMinutes() {
        if (isInProgress && startTime > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            return (int) (elapsed / 60000);
        }
        return 0;
    }

    // Get total time including current session
    public int getTotalTrackedMinutes() {
        return actualDuration + getCurrentSessionMinutes();
    }

    // Get formatted duration
    public String getFormattedDuration() {
        int duration = estimatedDuration;
        if (duration < 60) {
            return duration + " min";
        } else {
            int hours = duration / 60;
            int mins = duration % 60;
            return hours + "h" + (mins > 0 ? " " + mins + "m" : "");
        }
    }

    // Get actual duration formatted
    public String getFormattedActualDuration() {
        if (actualDuration == 0) return "Not tracked";
        if (actualDuration < 60) {
            return actualDuration + " min";
        } else {
            int hours = actualDuration / 60;
            int mins = actualDuration % 60;
            return hours + "h" + (mins > 0 ? " " + mins + "m" : "");
        }
    }

    // Format scheduled date
    public String getFormattedScheduledDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(new Date(scheduledDate));
    }

    // Formatted time strings
    public String getFormattedCreatedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        return sdf.format(new Date(createdTimestamp));
    }

    public String getFormattedDueTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        return sdf.format(new Date(dueTimestamp));
    }

    public boolean isOverdue() {
        return !isCompleted && dueTimestamp < System.currentTimeMillis();
    }

    // Progress percentage based on time (can go over 100%)
    public int getProgress() {
        if (isCompleted) return 100;
        if (!isInProgress || startTime == 0) return 0;

        int totalMinutes = getTotalTrackedMinutes();
        // Return actual percentage even if over 100%
        return (totalMinutes * 100) / Math.max(1, estimatedDuration);
    }

    // Check if over estimated time
    public boolean isOverEstimate() {
        return getTotalTrackedMinutes() > estimatedDuration;
    }

    // All getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getDueTimestamp() { return dueTimestamp; }
    public void setDueTimestamp(long dueTimestamp) {
        this.dueTimestamp = dueTimestamp;
    }

    public long getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(long scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public int getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getActualDuration() { return actualDuration; }
    public void setActualDuration(int actualDuration) {
        this.actualDuration = actualDuration;
    }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
        // Stop tracking when completing
        if (completed && isInProgress) {
            stopTask();
        }
    }

    public boolean isImmediate() { return isImmediate; }
    public void setImmediate(boolean immediate) { isImmediate = immediate; }

    public boolean isInProgress() { return isInProgress; }
    public void setInProgress(boolean inProgress) { isInProgress = inProgress; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getColorTag() { return colorTag; }
    public void setColorTag(String colorTag) { this.colorTag = colorTag; }
}




//package com.tannazetm.dailytasktracker;
//
//import androidx.room.Entity;
//import androidx.room.PrimaryKey;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//@Entity(tableName = "tasks")
//public class Task {
//    @PrimaryKey(autoGenerate = true)
//    private int id;
//
//    private String title;
//    private String description;
//    private long createdTimestamp;
//    private long dueTimestamp;
//    private long scheduledDate; // Date task is scheduled for
//    private long startTime; // When task actually started
//    private long endTime; // When task actually finished
//    private int estimatedDuration; // Duration in minutes
//    private int actualDuration; // Actual duration taken
//    private boolean isCompleted;
//    private boolean isImmediate;
//    private boolean isInProgress; // Currently being worked on
//    private int priority; // 1=Low, 2=Medium, 3=High
//    private String category;
//    private String colorTag; // For visual organization
//
//    // Constructor for new tasks
//    public Task(String title, String description, long dueTimestamp, int estimatedDuration) {
//        this.title = title;
//        this.description = description;
//        this.createdTimestamp = System.currentTimeMillis();
//        this.dueTimestamp = dueTimestamp;
//        this.scheduledDate = dueTimestamp;
//        this.estimatedDuration = estimatedDuration;
//        this.actualDuration = 0;
//        this.startTime = 0;
//        this.endTime = 0;
//        this.isCompleted = false;
//        this.isImmediate = false;
//        this.isInProgress = false;
//        this.priority = 2;
//        this.category = "General";
//        this.colorTag = "#7C3AED";
//    }
//
//    // Simple constructor
//    public Task(String title) {
//        this(title, "", System.currentTimeMillis() + (24 * 60 * 60 * 1000), 30);
//    }
//
//    // Empty constructor for Room
//    public Task() {
//        this.createdTimestamp = System.currentTimeMillis();
//        this.estimatedDuration = 30;
//    }
//
//    // Start tracking time
//    public void startTask() {
//        this.startTime = System.currentTimeMillis();
//        this.isInProgress = true;
//    }
//
//    // Stop tracking time
//    public void stopTask() {
//        if (startTime > 0) {
//            this.endTime = System.currentTimeMillis();
//            this.actualDuration = (int) ((endTime - startTime) / 60000); // Convert to minutes
//            this.isInProgress = false;
//        }
//    }
//
//    // Get formatted duration
//    public String getFormattedDuration() {
//        int duration = estimatedDuration;
//        if (duration < 60) {
//            return duration + " min";
//        } else {
//            int hours = duration / 60;
//            int mins = duration % 60;
//            return hours + "h " + (mins > 0 ? mins + "m" : "");
//        }
//    }
//
//    // Get actual duration formatted
//    public String getFormattedActualDuration() {
//        if (actualDuration == 0) return "Not tracked";
//        if (actualDuration < 60) {
//            return actualDuration + " min";
//        } else {
//            int hours = actualDuration / 60;
//            int mins = actualDuration % 60;
//            return hours + "h " + (mins > 0 ? mins + "m" : "");
//        }
//    }
//
//    // Format scheduled date
//    public String getFormattedScheduledDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
//        return sdf.format(new Date(scheduledDate));
//    }
//
//    // Formatted time strings
//    public String getFormattedCreatedTime() {
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//        return sdf.format(new Date(createdTimestamp));
//    }
//
//    public String getFormattedDueTime() {
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
//        return sdf.format(new Date(dueTimestamp));
//    }
//
//    public boolean isOverdue() {
//        return !isCompleted && dueTimestamp < System.currentTimeMillis();
//    }
//
//    // Progress percentage based on time
//    public int getProgress() {
//        if (isCompleted) return 100;
//        if (!isInProgress || startTime == 0) return 0;
//
//        long elapsed = System.currentTimeMillis() - startTime;
//        int elapsedMinutes = (int) (elapsed / 60000);
//        return Math.min(100, (elapsedMinutes * 100) / Math.max(1, estimatedDuration));
//    }
//
//    // All getters and setters
//    public int getId() { return id; }
//    public void setId(int id) { this.id = id; }
//
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public long getCreatedTimestamp() { return createdTimestamp; }
//    public void setCreatedTimestamp(long createdTimestamp) {
//        this.createdTimestamp = createdTimestamp;
//    }
//
//    public long getDueTimestamp() { return dueTimestamp; }
//    public void setDueTimestamp(long dueTimestamp) {
//        this.dueTimestamp = dueTimestamp;
//    }
//
//    public long getScheduledDate() { return scheduledDate; }
//    public void setScheduledDate(long scheduledDate) {
//        this.scheduledDate = scheduledDate;
//    }
//
//    public long getStartTime() { return startTime; }
//    public void setStartTime(long startTime) { this.startTime = startTime; }
//
//    public long getEndTime() { return endTime; }
//    public void setEndTime(long endTime) { this.endTime = endTime; }
//
//    public int getEstimatedDuration() { return estimatedDuration; }
//    public void setEstimatedDuration(int estimatedDuration) {
//        this.estimatedDuration = estimatedDuration;
//    }
//
//    public int getActualDuration() { return actualDuration; }
//    public void setActualDuration(int actualDuration) {
//        this.actualDuration = actualDuration;
//    }
//
//    public boolean isCompleted() { return isCompleted; }
//    public void setCompleted(boolean completed) { isCompleted = completed; }
//
//    public boolean isImmediate() { return isImmediate; }
//    public void setImmediate(boolean immediate) { isImmediate = immediate; }
//
//    public boolean isInProgress() { return isInProgress; }
//    public void setInProgress(boolean inProgress) { isInProgress = inProgress; }
//
//    public int getPriority() { return priority; }
//    public void setPriority(int priority) { this.priority = priority; }
//
//    public String getCategory() { return category; }
//    public void setCategory(String category) { this.category = category; }
//
//    public String getColorTag() { return colorTag; }
//    public void setColorTag(String colorTag) { this.colorTag = colorTag; }
//}