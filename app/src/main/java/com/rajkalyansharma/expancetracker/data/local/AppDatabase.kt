package com.rajkalyansharma.expancetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rajkalyansharma.expancetracker.data.local.dao.GoalDao
import com.rajkalyansharma.expancetracker.data.local.dao.NotificationDao
import com.rajkalyansharma.expancetracker.data.local.dao.TransactionDao
import com.rajkalyansharma.expancetracker.data.local.entity.Goal
import com.rajkalyansharma.expancetracker.data.local.entity.Notification
import com.rajkalyansharma.expancetracker.data.local.entity.Transaction

@Database(entities = [Transaction::class, Goal::class, Notification::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
