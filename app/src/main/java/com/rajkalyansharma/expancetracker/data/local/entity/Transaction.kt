package com.rajkalyansharma.expancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Long,
    val note: String
)
