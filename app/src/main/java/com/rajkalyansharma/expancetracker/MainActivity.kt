package com.rajkalyansharma.expancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajkalyansharma.expancetracker.ui.MainScreen
import com.rajkalyansharma.expancetracker.ui.TransactionViewModel
import com.rajkalyansharma.expancetracker.ui.theme.ExpanceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TransactionViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            ExpanceTrackerTheme(darkTheme = isDarkMode) {
                MainScreen(viewModel)
            }
        }
    }
}
