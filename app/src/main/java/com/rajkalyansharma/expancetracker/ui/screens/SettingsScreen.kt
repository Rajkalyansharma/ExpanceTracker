package com.rajkalyansharma.expancetracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajkalyansharma.expancetracker.ui.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: TransactionViewModel, onBack: () -> Unit) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val currencySymbol by viewModel.selectedCurrency.collectAsState()
    val context = LocalContext.current
    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header removed as requested

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SettingsSection(title = "Preference") {
                    SettingToggleItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Switch between light and dark themes",
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                    SettingToggleItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Daily Reminders",
                        subtitle = "Track your daily expance to complete your goal 🎯",
                        checked = notificationsEnabled,
                        onCheckedChange = { 
                            viewModel.toggleNotifications(it)
                            if (it) {
                                com.rajkalyansharma.expancetracker.util.NotificationHelper.showNotification(
                                    context,
                                    "Daily Reminder Enabled! 🔔",
                                    "Track your daily expance to complete your goal 🎯"
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSection(title = "Security & Data") {
                    SettingToggleItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Use Fingerprint or Face ID to unlock",
                        checked = biometricEnabled,
                        onCheckedChange = { 
                            if (it) {
                                val activity = context as? androidx.fragment.app.FragmentActivity
                                if (activity != null && com.rajkalyansharma.expancetracker.util.BiometricHelper.isBiometricAvailable(context)) {
                                    com.rajkalyansharma.expancetracker.util.BiometricHelper.showBiometricPrompt(
                                        activity,
                                        onSuccess = { 
                                            viewModel.toggleBiometric(true)
                                            Toast.makeText(context, "Biometric Lock Activated", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Biometric not available on this device", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.toggleBiometric(false)
                                Toast.makeText(context, "Biometric Lock Deactivated", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    SettingClickItem(
                        icon = Icons.Default.FileDownload,
                        title = "Export Data",
                        subtitle = "Download your report in CSV format",
                        onClick = { 
                            viewModel.exportToCsv()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSection(title = "More") {
                    SettingClickItem(
                        icon = Icons.Default.Language,
                        title = "Currency",
                        subtitle = "Selected: $currencySymbol",
                        onClick = { showCurrencyDialog = true }
                    )
                    SettingClickItem(
                        icon = Icons.Default.Info,
                        title = "Help & Support",
                        subtitle = "Read our FAQ or contact support",
                        onClick = { /* Navigate to support */ }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "ExpanceTracker v1.2.4",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Made with ❤️ for better finance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentSymbol = currencySymbol,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { symbol ->
                viewModel.setCurrency(symbol)
                showCurrencyDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionDialog(
    currentSymbol: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf(
        "USD" to "$",
        "INR" to "₹",
        "EUR" to "€",
        "GBP" to "£",
        "JPY" to "¥"
    )

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Currency",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                currencies.forEach { (name, symbol) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(symbol) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "$name ($symbol)", style = MaterialTheme.typography.bodyLarge)
                        if (symbol == currentSymbol) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Cancel")
                }
            }
        }
    }
}


@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (contentColor == MaterialTheme.colorScheme.error) contentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (contentColor == MaterialTheme.colorScheme.error) contentColor else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = contentColor)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.ChevronRight, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}
