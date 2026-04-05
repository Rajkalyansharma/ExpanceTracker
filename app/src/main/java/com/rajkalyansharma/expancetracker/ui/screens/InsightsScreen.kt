package com.rajkalyansharma.expancetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajkalyansharma.expancetracker.data.local.entity.TransactionType
import com.rajkalyansharma.expancetracker.ui.TransactionViewModel
import com.rajkalyansharma.expancetracker.util.formatCurrency

@Composable
fun InsightsScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currencySymbol by viewModel.selectedCurrency.collectAsState()
    val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
    val incomeTransactions = transactions.filter { it.type == TransactionType.INCOME }
    
    val categoryTotals = expenseTransactions.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
    val totalExpense = expenseTransactions.sumOf { it.amount }
    val totalIncome = incomeTransactions.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Detailed breakdown of your spending habits",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight(0.7f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Analytics,
                                    null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Insight Engine Offline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Add some transactions to see the magic!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                SummarySection(totalIncome, totalExpense, currencySymbol)
            }

            item {
                AnimatedDonutChartCard(categoryTotals, totalExpense, currencySymbol)
            }

            item {
                Text(
                    text = "Top Spending Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(categoryTotals.toList().sortedByDescending { it.second }) { (category, amount) ->
                AnimatedInsightItem(category, amount, totalExpense, currencySymbol)
            }
        }
    }
}

@Composable
fun SummarySection(income: Double, expense: Double, currencySymbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InsightCard(
            title = "Total Income",
            amount = income,
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            currencySymbol = currencySymbol
        )
        InsightCard(
            title = "Total Expense",
            amount = expense,
            icon = Icons.Default.TrendingDown,
            color = Color(0xFFFF5252),
            modifier = Modifier.weight(1f),
            currencySymbol = currencySymbol
        )
    }
}

@Composable
fun InsightCard(title: String, amount: Double, icon: ImageVector, color: Color, modifier: Modifier, currencySymbol: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                amount.formatCurrency(currencySymbol),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun AnimatedDonutChartCard(categoryTotals: Map<String, Double>, total: Double, currencySymbol: String) {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFFF43F5E), Color(0xFF10B981),
        Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF06B6D4)
    )

    var animationPlayed by remember { mutableStateOf(false) }
    val animateSweep by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "ChartAnimation"
    )

    LaunchedEffect(Unit) { animationPlayed = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        var startAngle = -90f
                        categoryTotals.values.forEachIndexed { index, amount ->
                            val sweepAngle = (amount / total * 360).toFloat() * animateSweep
                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 35f, cap = StrokeCap.Round),
                            )
                            startAngle += (amount / total * 360).toFloat()
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            total.formatCurrency(currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoryTotals.keys.take(5).forEachIndexed { index, category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors[index % colors.size]))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                category, 
                                style = MaterialTheme.typography.bodySmall, 
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedInsightItem(category: String, amount: Double, total: Double, currencySymbol: String) {
    val progress = (amount / total).toFloat()
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(Unit) { animationPlayed = true }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            getCategoryIconForInsight(category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    amount.formatCurrency(currencySymbol),
                    fontWeight = FontWeight.ExtraBold, 
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% of spending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (progress > 0.5) "High Spend" else if (progress > 0.2) "Moderate" else "Low Spend",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (progress > 0.5) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun getCategoryIconForInsight(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Transport" -> Icons.Default.DirectionsCar
        "Shopping" -> Icons.Default.ShoppingBag
        "Entertainment" -> Icons.Default.Movie
        "Bills" -> Icons.Default.Receipt
        "Health" -> Icons.Default.MedicalServices
        "Salary" -> Icons.Default.Payments
        "Freelance" -> Icons.Default.Work
        "Gift" -> Icons.Default.CardGiftcard
        "Investment" -> Icons.Default.TrendingUp
        else -> Icons.Default.Category
    }
}
