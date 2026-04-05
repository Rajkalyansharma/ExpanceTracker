package com.rajkalyansharma.expancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: Long? = null,
    val isCompleted: Boolean = false
)
