package com.tannazetm.dailytasktracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {

    // Get all tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueTimestamp ASC")
    List<Task> getAllTasks();

    // Get today's tasks
    @Query("SELECT * FROM tasks WHERE date(dueTimestamp/1000, 'unixepoch') = date('now') ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getTodayTasks();

    // Get tomorrow's tasks
    @Query("SELECT * FROM tasks WHERE date(dueTimestamp/1000, 'unixepoch') = date('now', '+1 day') ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getTomorrowTasks();

    // Get this week's tasks
    @Query("SELECT * FROM tasks WHERE date(dueTimestamp/1000, 'unixepoch') BETWEEN date('now') AND date('now', '+7 days') ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getThisWeekTasks();

    // Get tasks by date range
    @Query("SELECT * FROM tasks WHERE dueTimestamp BETWEEN :startDate AND :endDate ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getTasksByDateRange(long startDate, long endDate);

    // Get active tasks (not completed)
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getActiveTasks();

    // Get completed tasks
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY dueTimestamp DESC")
    List<Task> getCompletedTasks();

    // Get overdue tasks
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueTimestamp < :currentTime ORDER BY dueTimestamp ASC")
    List<Task> getOverdueTasks(long currentTime);

    // Get tasks by priority
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY isCompleted ASC, dueTimestamp ASC")
    List<Task> getTasksByPriority(int priority);

    // Get tasks with filters - ALL COMBINATIONS
    // Status: All, Date: Custom, Priority: Specific
    @Query("SELECT * FROM tasks WHERE dueTimestamp BETWEEN :startDate AND :endDate AND priority = :priority ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getTasksByDateAndPriority(long startDate, long endDate, int priority);

    // Status: Active, Date: Custom, Priority: All
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueTimestamp BETWEEN :startDate AND :endDate ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getActiveTasksByDate(long startDate, long endDate);

    // Status: Completed, Date: Custom, Priority: All
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND dueTimestamp BETWEEN :startDate AND :endDate ORDER BY dueTimestamp DESC")
    List<Task> getCompletedTasksByDate(long startDate, long endDate);

    // Status: Active, Date: Custom, Priority: Specific
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueTimestamp BETWEEN :startDate AND :endDate AND priority = :priority ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getActiveTasksByDateAndPriority(long startDate, long endDate, int priority);

    // Status: Completed, Date: Custom, Priority: Specific
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND dueTimestamp BETWEEN :startDate AND :endDate AND priority = :priority ORDER BY dueTimestamp DESC")
    List<Task> getCompletedTasksByDateAndPriority(long startDate, long endDate, int priority);

    // Status: Overdue, Date: ignored, Priority: All
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueTimestamp < :currentTime ORDER BY dueTimestamp ASC")
    List<Task> getOverdueTasksAll(long currentTime);

    // Status: Overdue, Priority: Specific
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueTimestamp < :currentTime AND priority = :priority ORDER BY dueTimestamp ASC")
    List<Task> getOverdueTasksByPriority(long currentTime, int priority);

    // Status: Active, Priority: Specific
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND priority = :priority ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getActiveTasksByPriority(int priority);

    // Status: Completed, Priority: Specific
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND priority = :priority ORDER BY dueTimestamp DESC")
    List<Task> getCompletedTasksByPriority(int priority);

    // Get task by ID
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(int taskId);

    // Insert new task
    @Insert
    long insertTask(Task task);

    // Update existing task
    @Update
    void updateTask(Task task);

    // Delete task
    @Delete
    void deleteTask(Task task);

    // Count immediate tasks that aren't completed
    @Query("SELECT COUNT(*) FROM tasks WHERE isImmediate = 1 AND isCompleted = 0")
    int getImmediateTaskCount();

    // Count completed tasks
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    int getCompletedCount();

    // Get today's completed count
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND date(dueTimestamp/1000, 'unixepoch') = date('now')")
    int getTodayCompletedCount();
}





//package com.tannazetm.dailytasktracker;
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Update;
//import java.util.List;
//
//@Dao
//public interface TaskDao {
//
//    // Get all tasks
//    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueTimestamp ASC")
//    List<Task> getAllTasks();
//
//    // Get today's tasks (this method was missing!)
//    @Query("SELECT * FROM tasks WHERE date(dueTimestamp/1000, 'unixepoch') = date('now') ORDER BY isImmediate DESC, dueTimestamp ASC")
//    List<Task> getTodayTasks();
//
//    // Get task by ID
//    @Query("SELECT * FROM tasks WHERE id = :taskId")
//    Task getTaskById(int taskId);
//
//    // Insert new task
//    @Insert
//    long insertTask(Task task);
//
//    // Update existing task
//    @Update
//    void updateTask(Task task);
//
//    // Delete task
//    @Delete
//    void deleteTask(Task task);
//
//    // Count immediate tasks that aren't completed
//    @Query("SELECT COUNT(*) FROM tasks WHERE isImmediate = 1 AND isCompleted = 0")
//    int getImmediateTaskCount();
//
//    // Count completed tasks
//    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
//    int getCompletedCount();
//
//    // Get today's completed count (this method was missing!)
//    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND date(dueTimestamp/1000, 'unixepoch') = date('now')")
//    int getTodayCompletedCount();
//}