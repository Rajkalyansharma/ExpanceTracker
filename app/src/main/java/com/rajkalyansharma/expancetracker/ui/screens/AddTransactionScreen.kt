package com.rajkalyansharma.expancetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rajkalyansharma.expancetracker.data.local.entity.TransactionType
import com.rajkalyansharma.expancetracker.ui.TransactionViewModel

@Composable
fun AddTransactionScreen(viewModel: TransactionViewModel, onTransactionAdded: () -> Unit) {
    val currencySymbol by viewModel.selectedCurrency.collectAsState()
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    val categories = if (type == TransactionType.EXPENSE) {
        listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Other")
    } else {
        listOf("Salary", "Freelance", "Gift", "Investment", "Other")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Create New Record",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 0.dp)
        )

        // Type Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            TransactionTypeTab(
                selected = type == TransactionType.EXPENSE,
                text = "Expense",
                color = Color(0xFFFF5252),
                modifier = Modifier.weight(1f),
                onClick = { type = TransactionType.EXPENSE }
            )
            TransactionTypeTab(
                selected = type == TransactionType.INCOME,
                text = "Income",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = { type = TransactionType.INCOME }
            )
        }

        // Amount Input
        Column {
            Text(
                text = "Amount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                placeholder = { Text("0.00") },
                prefix = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                singleLine = true
            )
        }

        // Category Chips
        Column {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Add a note...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
        )

        Button(
            onClick = {
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                if (amountDouble > 0 && category.isNotBlank()) {
                    viewModel.addTransaction(
                        amount = amountDouble,
                        type = type,
                        category = category,
                        date = System.currentTimeMillis(),
                        note = note
                    )
                    onTransactionAdded()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp), // Reduced bottom padding to avoid clashing with bottom bar
            shape = RoundedCornerShape(16.dp),
            enabled = amount.isNotBlank() && category.isNotBlank()
        ) {
            Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionTypeTab(
    selected: Boolean,
    text: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        color = if (selected) color else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
