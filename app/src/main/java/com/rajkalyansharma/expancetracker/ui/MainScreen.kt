package com.rajkalyansharma.expancetracker.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rajkalyansharma.expancetracker.navigation.Screen
import com.rajkalyansharma.expancetracker.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TransactionViewModel) {
    val navController = rememberNavController()
    
    val navItems = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.AddTransaction,
        Screen.Insights,
        Screen.Goals
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    
    val currentScreen = navItems.find { it.route == currentRoute } ?: Screen.Home

    val isTopLevelScreen = navItems.any { it.route == currentRoute }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (currentRoute != Screen.Settings.route && currentRoute != Screen.Home.route && currentRoute != Screen.Notifications.route) {
                    TopAppBar(
                        title = { 
                            Text(
                                text = when {
                                    currentRoute?.startsWith("edit_transaction") == true -> "Edit Transaction"
                                    currentRoute == Screen.AddTransaction.route -> "New Transaction"
                                    else -> currentScreen.title
                                },
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        navigationIcon = {
                            if (!isTopLevelScreen || currentRoute?.startsWith("edit_transaction") == true) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        actions = {
                            // Moved to HomeScreen welcome header
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                }
            },
            bottomBar = {
                if (isTopLevelScreen) {
                    GlassmorphicBottomBar(
                        navController = navController,
                        items = navItems,
                        currentDestination = currentDestination
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { 
                    fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))
                },
                exitTransition = { 
                    fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 1.05f, animationSpec = tween(400))
                },
                popEnterTransition = { 
                    fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 1.05f, animationSpec = tween(400))
                },
                popExitTransition = { 
                    fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.95f, animationSpec = tween(400))
                }
            ) {
                composable(Screen.Home.route) { 
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    ) 
                }
                composable(Screen.Transactions.route) { 
                    TransactionsScreen(
                        viewModel = viewModel,
                        onEditTransaction = { id -> 
                            navController.navigate(Screen.EditTransaction.createRoute(id))
                        }
                    ) 
                }
                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(viewModel, onTransactionAdded = {
                        navController.popBackStack()
                    })
                }
                composable(Screen.Insights.route) { InsightsScreen(viewModel) }
                composable(Screen.Goals.route) { GoalsScreen(viewModel) }
                composable(Screen.Settings.route) { 
                    SettingsScreen(viewModel, onBack = { navController.popBackStack() }) 
                }
                composable(Screen.Notifications.route) {
                    NotificationsScreen(viewModel, onBack = { navController.popBackStack() })
                }
                composable(
                    route = Screen.EditTransaction.route,
                    arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: return@composable
                    EditTransactionScreen(
                        transactionId = transactionId,
                        viewModel = viewModel,
                        onTransactionUpdated = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassmorphicBottomBar(
    navController: androidx.navigation.NavHostController,
    items: List<Screen>,
    currentDestination: androidx.navigation.NavDestination?
) {
    val selectedIndex = items.indexOfFirst { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        val itemWidth = this.maxWidth / items.size
        
        // Smoothly fade the indicator when moving to/from the Add button
        val showIndicator = items[selectedIndex] != Screen.AddTransaction
        val indicatorAlpha by animateFloatAsState(
            targetValue = if (showIndicator) 1f else 0f,
            animationSpec = tween(300),
            label = "alpha"
        )

        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "indicator"
        )

        // Simple sliding dot indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(4.dp)
                    .graphicsLayer { alpha = indicatorAlpha }
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, screen ->
                val selected = index == selectedIndex
                val isAddButton = screen == Screen.AddTransaction

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isAddButton) {
                        // Simple Central Add Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            screen.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.title,
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
