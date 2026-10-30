package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object WhatsAppSharer {

    /**
     * مشاركة صورة الإيصال عبر واتساب (إن كان مثبتاً) أو عبر أي تطبيق مشاركة آخر.
     *
     * @param phoneNumber رقم هاتف العميل بالصيغة الدولية (اختياري) — عند توفره
     * يتم توجيه المشاركة إلى محادثته مباشرة داخل واتساب.
     */
    fun shareReceiptImage(context: Context, receiptBitmap: Bitmap, phoneNumber: String?, captionText: String) {
        try {
            val cachePath = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cachePath, "invoice_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { fos ->
                receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }

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

            if (isPackageInstalled(context, "com.whatsapp")) {
                intent.setPackage("com.whatsapp")
                val formattedPhone = phoneNumber?.filter { it.isDigit() }
                if (!formattedPhone.isNullOrBlank()) {
                    intent.putExtra("jid", "$formattedPhone@s.whatsapp.net")
                }
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة عبر"))
            }
        } catch (e: Exception) {
            shareTextOnly(context, captionText)
        }
    }

    /** خطة بديلة: مشاركة النص فقط عند فشل مشاركة الصورة لأي سبب */
    private fun shareTextOnly(context: Context, captionText: String) {
        try {
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, captionText)
            }
            context.startActivity(Intent.createChooser(textIntent, "مشاركة الفاتورة"))
        } catch (_: Exception) {
            // لا يوجد أي تطبيق يستطيع استقبال المشاركة — نتجاهل بأمان
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
