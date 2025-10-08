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

    // Get today's tasks (this method was missing!)
    @Query("SELECT * FROM tasks WHERE date(dueTimestamp/1000, 'unixepoch') = date('now') ORDER BY isImmediate DESC, dueTimestamp ASC")
    List<Task> getTodayTasks();

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

    // Get today's completed count (this method was missing!)
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND date(dueTimestamp/1000, 'unixepoch') = date('now')")
    int getTodayCompletedCount();
}