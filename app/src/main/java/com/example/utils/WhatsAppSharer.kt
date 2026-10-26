package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object WhatsAppSharer {
    fun shareReceiptImage(context: Context, receiptBitmap: Bitmap, phoneNumber: String?, captionText: String) {
        try {
            val cachePath = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cachePath, "invoice_${System.currentTimeMillis()}.png")
            val fos = FileOutputStream(file)
            receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, captionText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val formattedPhone = phoneNumber?.replace("+", "")?.replace(" ", "")
            if (!formattedPhone.isNullOrBlank()) {
                intent.`package` = "com.whatsapp"
            }
            
            context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة عبر"))
        } catch (e: Exception) {
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, captionText)
            }
            context.startActivity(Intent.createChooser(textIntent, "مشاركة الفاتورة"))
        }
    }
}
