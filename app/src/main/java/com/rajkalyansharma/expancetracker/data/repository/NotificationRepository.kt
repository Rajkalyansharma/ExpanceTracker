package com.rajkalyansharma.expancetracker.data.repository

import com.rajkalyansharma.expancetracker.data.local.dao.NotificationDao
import com.rajkalyansharma.expancetracker.data.local.entity.Notification
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    val unreadCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun insert(notification: Notification) {
        notificationDao.insert(notification)
    }

    suspend fun update(notification: Notification) {
        notificationDao.update(notification)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun delete(notification: Notification) {
        notificationDao.delete(notification)
    }

    suspend fun deleteAll() {
        notificationDao.deleteAll()
    }
}
