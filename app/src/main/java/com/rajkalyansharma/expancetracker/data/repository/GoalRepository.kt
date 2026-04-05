package com.rajkalyansharma.expancetracker.data.repository

import com.rajkalyansharma.expancetracker.data.local.dao.GoalDao
import com.rajkalyansharma.expancetracker.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()

    suspend fun insert(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun update(goal: Goal) {
        goalDao.updateGoal(goal)
    }

    suspend fun delete(goal: Goal) {
        goalDao.deleteGoal(goal)
    }
}
