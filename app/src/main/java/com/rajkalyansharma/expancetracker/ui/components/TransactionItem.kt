package com.rajkalyansharma.expancetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajkalyansharma.expancetracker.data.local.entity.Transaction
import com.rajkalyansharma.expancetracker.data.local.entity.TransactionType
import com.rajkalyansharma.expancetracker.util.formatCurrency
import com.rajkalyansharma.expancetracker.util.formatDate

@Composable
fun TransactionItem(transaction: Transaction, currencySymbol: String = "$") {
    val categoryIcon = getCategoryIcon(transaction.category)
    val color = if (transaction.type == TransactionType.EXPENSE) Color(0xFFFF5252) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = transaction.date.formatDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Amount
            Text(
                text = (if (transaction.type == TransactionType.EXPENSE) "-" else "+") + transaction.amount.formatCurrency(currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
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
