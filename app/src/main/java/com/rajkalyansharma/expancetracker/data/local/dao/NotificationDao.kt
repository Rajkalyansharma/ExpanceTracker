package com.rajkalyansharma.expancetracker.data.local.dao

import androidx.room.*
import com.rajkalyansharma.expancetracker.data.local.entity.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification)

    @Update
    suspend fun update(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Delete
    suspend fun delete(notification: Notification)
    
    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
