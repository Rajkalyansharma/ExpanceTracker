package com.rajkalyansharma.expancetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajkalyansharma.expancetracker.data.local.entity.TransactionType
import com.rajkalyansharma.expancetracker.ui.TransactionViewModel
import com.rajkalyansharma.expancetracker.ui.components.TransactionItem
import com.rajkalyansharma.expancetracker.util.formatCurrency

@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()
    val income by viewModel.totalIncome.collectAsState()
    val expense by viewModel.totalExpense.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val goals by viewModel.allGoals.collectAsState(initial = emptyList())
    val currencySymbol by viewModel.selectedCurrency.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    val scrollState = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Welcome Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Manage your money",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToNotifications) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                ) {
                                    Text(unreadCount.toString(), fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Balance Card with Animation
            item {
                AnimatedBalanceCard(balance, income, expense, currencySymbol)
            }

            // 2. Savings Progress Overview
            if (goals.isNotEmpty()) {
                item {
                    SavingsOverviewCard(goals.first(), currencySymbol)
                }
            }

            // 3. Spending Chart / Indicator
            item {
                SpendingTrendCard(income, expense)
            }

            // 4. Recent History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* Navigate to All */ }) {
                        Text("See All")
                    }
                }
            }
            
            if (transactions.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(transactions.take(5), key = { it.id }) { transaction ->
                    TransactionItem(transaction, currencySymbol)
                }
            }
        }
    }
}

@Composable
fun AnimatedBalanceCard(balance: Double, income: Double, expense: Double, currencySymbol: String) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(1000)) + expandVertically(tween(1000), expandFrom = Alignment.Top)
    ) {
        val gradient = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(gradient)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Total Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Text(
                        text = balance.formatCurrency(currencySymbol),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BalanceSummaryItem(
                            label = "Income",
                            amount = income,
                            icon = Icons.Default.ArrowUpward,
                            color = Color(0xFF4CAF50),
                            currencySymbol = currencySymbol
                        )
                        BalanceSummaryItem(
                            label = "Expense",
                            amount = expense,
                            icon = Icons.Default.ArrowDownward,
                            color = Color(0xFFFF5252),
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceSummaryItem(label: String, amount: Double, icon: ImageVector, color: Color, currencySymbol: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            Text(
                text = amount.formatCurrency(currencySymbol),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SavingsOverviewCard(goal: com.rajkalyansharma.expancetracker.data.local.entity.Goal, currencySymbol: String) {
    val progress = (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Savings Goal: ${goal.title}", fontWeight = FontWeight.Bold)
                }
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SpendingTrendCard(income: Double, expense: Double) {
    val ratio = if (income > 0) (expense / income).toFloat().coerceIn(0f, 1.2f) else 0f
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "Ratio"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedRatio.coerceIn(0f, 1f) },
                    modifier = Modifier.size(80.dp),
                    color = if (ratio > 0.9f) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(ratio * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Used", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column {
                Text(
                    text = "Spending Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        ratio == 0f -> "No transactions this month."
                        ratio < 0.5f -> "You're doing great! Keep it up."
                        ratio < 0.9f -> "You're approaching your limit."
                        else -> "Warning: High spending detected!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.TrendingUp, 
            contentDescription = null, 
            modifier = Modifier.size(64.dp), 
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your journey starts here", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Add a transaction to see your insights", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
