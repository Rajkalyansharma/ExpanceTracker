package com.rajkalyansharma.expancetracker.util

import java.text.SimpleDateFormat
import java.util.*

fun Long.formatDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

fun Double.formatCurrency(symbol: String = "$"): String {
    return "$symbol ${String.format("%.2f", this)}"
}
