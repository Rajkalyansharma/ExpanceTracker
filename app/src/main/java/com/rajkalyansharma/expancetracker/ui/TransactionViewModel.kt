package com.rajkalyansharma.expancetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajkalyansharma.expancetracker.data.local.AppDatabase
import com.rajkalyansharma.expancetracker.data.local.entity.Goal
import com.rajkalyansharma.expancetracker.data.local.entity.Notification
import com.rajkalyansharma.expancetracker.data.local.entity.Transaction
import com.rajkalyansharma.expancetracker.data.local.entity.TransactionType
import com.rajkalyansharma.expancetracker.data.repository.GoalRepository
import com.rajkalyansharma.expancetracker.data.repository.NotificationRepository
import com.rajkalyansharma.expancetracker.data.repository.TransactionRepository
import com.rajkalyansharma.expancetracker.util.NotificationHelper
import androidx.core.content.edit
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val transactionRepository: TransactionRepository
    private val goalRepository: GoalRepository
    private val notificationRepository: NotificationRepository
    private val prefs = application.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)

    val allTransactions: StateFlow<List<Transaction>>
    val allGoals: StateFlow<List<Goal>>
    val allNotifications: StateFlow<List<Notification>>
    val unreadNotificationCount: StateFlow<Int>

    // Settings State
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(prefs.getBoolean("biometric", false))
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(prefs.getString("currency", "$") ?: "$")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        transactionRepository = TransactionRepository(database.transactionDao())
        goalRepository = GoalRepository(database.goalDao())
        notificationRepository = NotificationRepository(database.notificationDao())

        allTransactions = transactionRepository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGoals = goalRepository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allNotifications = notificationRepository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        unreadNotificationCount = notificationRepository.unreadCount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        // Observe goals to trigger notifications when reached
        viewModelScope.launch {
            combine(allTransactions, allGoals) { txs, goals ->
                goals.forEach { goal ->
                    if (goal.savedAmount >= goal.targetAmount && !goal.isCompleted && notificationsEnabled.value) {
                        val title = "Goal Achieved! 🎉"
                        val message = "Congratulations! You've reached your goal: ${goal.title}"
                        
                        NotificationHelper.showNotification(context, title, message)
                        addInAppNotification(title, message)
                    }
                }
            }.collect()
        }
    }

    // Transactions
    val balance: StateFlow<Double> = allTransactions.map { transactions ->
        transactions.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = allTransactions.map { transactions ->
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = allTransactions.map { transactions ->
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(amount: Double, type: TransactionType, category: String, date: Long, note: String) {
        viewModelScope.launch {
            transactionRepository.insert(Transaction(amount = amount, type = type, category = category, date = date, note = note))
            
            // Check and update goals if this transaction helps any goal
            if (type == TransactionType.INCOME) {
                val goals = allGoals.value
                goals.forEach { goal ->
                    if (!goal.isCompleted) {
                        val newSavedAmount = goal.savedAmount + amount
                        val isNowCompleted = newSavedAmount >= goal.targetAmount
                        updateGoal(goal.copy(
                            savedAmount = newSavedAmount,
                            isCompleted = isNowCompleted
                        ))
                    }
                }
            }

            if (notificationsEnabled.value) {
                val title = "Transaction Added"
                val message = "A new $category ${type.name.lowercase()} of $amount was recorded."
                NotificationHelper.showNotification(context, title, message)
                addInAppNotification(title, message)
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.update(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }

    // Goals
    fun addGoal(title: String, targetAmount: Double) {
        viewModelScope.launch {
            goalRepository.insert(Goal(title = title, targetAmount = targetAmount))
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.update(goal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.delete(goal)
        }
    }

    // Settings
    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit { putBoolean("dark_mode", enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit { putBoolean("notifications", enabled) }
        if (enabled) {
            scheduleDailyReminder()
        } else {
            cancelDailyReminder()
        }
    }

    private fun scheduleDailyReminder() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.rajkalyansharma.expancetracker.worker.ReminderWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).setInitialDelay(calculateInitialDelay(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelDailyReminder() {
        androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("daily_reminder")
    }

    private fun calculateInitialDelay(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - System.currentTimeMillis()
    }

    fun toggleBiometric(enabled: Boolean) {
        _biometricEnabled.value = enabled
        prefs.edit { putBoolean("biometric", enabled) }
    }

    fun setCurrency(symbol: String) {
        _selectedCurrency.value = symbol
        prefs.edit { putString("currency", symbol) }
    }

    fun addMoneyToGoal(goal: Goal, amount: Double) {
        viewModelScope.launch {
            val newSavedAmount = goal.savedAmount + amount
            val isCompleted = newSavedAmount >= goal.targetAmount
            updateGoal(goal.copy(
                savedAmount = newSavedAmount,
                isCompleted = isCompleted
            ))
            
            // Deduct from total balance by adding an EXPENSE transaction
            addTransaction(
                amount = amount,
                type = TransactionType.EXPENSE,
                category = "Savings Goal",
                date = System.currentTimeMillis(),
                note = "Funding Goal: ${goal.title}"
            )
        }
    }

    fun exportToCsv() {
        com.rajkalyansharma.expancetracker.util.CsvExportHelper.exportTransactions(context, allTransactions.value)
    }

    // Notifications
    private fun addInAppNotification(title: String, message: String) {
        viewModelScope.launch {
            notificationRepository.insert(Notification(title = title, message = message))
        }
    }

    fun markNotificationAsRead(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.update(notification.copy(isRead = true))
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(notification: Notification) {
        viewModelScope.launch {
            notificationRepository.delete(notification)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAll()
        }
    }
}
