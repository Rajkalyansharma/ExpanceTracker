package com.rajkalyansharma.expancetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rajkalyansharma.expancetracker.data.local.entity.Transaction
import com.rajkalyansharma.expancetracker.data.local.entity.TransactionType
import com.rajkalyansharma.expancetracker.ui.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: Int,
    viewModel: TransactionViewModel,
    onTransactionUpdated: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currencySymbol by viewModel.selectedCurrency.collectAsState()
    val transaction = transactions.find { it.id == transactionId }

    if (transaction == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var category by remember { mutableStateOf(transaction.category) }
    var note by remember { mutableStateOf(transaction.note) }
    var type by remember { mutableStateOf(transaction.type) }
    
    val categories = if (type == TransactionType.EXPENSE) {
        listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Other")
    } else {
        listOf("Salary", "Freelance", "Gift", "Investment", "Other")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onTransactionUpdated) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

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
                label = { Text("Edit note...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (amountDouble > 0 && category.isNotBlank()) {
                        viewModel.updateTransaction(
                            transaction.copy(
                                amount = amountDouble,
                                type = type,
                                category = category,
                                note = note
                            )
                        )
                        onTransactionUpdated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = amount.isNotBlank() && category.isNotBlank()
            ) {
                Text("Update Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
