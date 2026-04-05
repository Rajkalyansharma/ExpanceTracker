package com.rajkalyansharma.expancetracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.rajkalyansharma.expancetracker.data.local.entity.Transaction
import java.io.File
import java.io.FileOutputStream

object CsvExportHelper {
    fun exportTransactions(context: Context, transactions: List<Transaction>) {
        val csvContent = StringBuilder()
        csvContent.append("ID,Amount,Type,Category,Date,Note\n")
        
        transactions.forEach {
            csvContent.append("${it.id},${it.amount},${it.type},${it.category},${it.date.formatDate()},\"${it.note}\"\n")
        }

        val fileName = "transactions_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            FileOutputStream(file).use {
                it.write(csvContent.toString().toByteArray())
            }
            shareFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export Transactions"))
    }
}
