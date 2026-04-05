package com.rajkalyansharma.expancetracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.List)
    object AddTransaction : Screen("add_transaction", "Add", Icons.Default.Add)
    object Insights : Screen("insights", "Insights", Icons.Default.Info)
    object Goals : Screen("goals", "Goals", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)
    object EditTransaction : Screen("edit_transaction/{transactionId}", "Edit Transaction") {
        fun createRoute(transactionId: Int) = "edit_transaction/$transactionId"
    }
}
