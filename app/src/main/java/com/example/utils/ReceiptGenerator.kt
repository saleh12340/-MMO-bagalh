package com.example.utils

import android.content.Context
import android.graphics.*
import com.example.data.local.dao.InvoiceWithCustomerAndItems
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptGenerator {
    fun generateReceiptBitmap(context: Context, data: InvoiceWithCustomerAndItems): Bitmap {
        val width = 384
        // هامش إضافي في الارتفاع لمنع قصّ محتوى الإيصال عند كثرة الأصناف
        val estimatedHeight = 700 + (data.items.size * 44)
        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

        var y = 45f
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("بقالة العزي للمواد الغذائية", width / 2f, y, paint)

        y += 32f
        paint.textSize = 20f
        paint.isFakeBoldText = false
        canvas.drawText("هاتف: 776425052", width / 2f, y, paint)

        y += 26f
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
        canvas.drawText("التاريخ: ${sdf.format(Date(data.invoice.createdAt))}", width / 2f, y, paint)

        y += 26f
        canvas.drawText("رقم الفاتورة: ${data.invoice.invoiceNumber}", width / 2f, y, paint)
        val customerName = data.customer?.name ?: "عميل نقدي"
        canvas.drawText("العميل: $customerName", width / 2f, y + 26f, paint)

        y += 56f
        paint.strokeWidth = 2f
        canvas.drawLine(10f, y, (width - 10).toFloat(), y, paint)

        y += 24f
        paint.textSize = 19f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("الصنف", (width - 15).toFloat(), y, paint)
        canvas.drawText("الكمية", (width - 190).toFloat(), y, paint)
        canvas.drawText("الإجمالي", 80f, y, paint)

        y += 12f
        canvas.drawLine(10f, y, (width - 10).toFloat(), y, paint)

        paint.isFakeBoldText = false
        for (item in data.items) {
            y += 34f
            canvas.drawText(item.productName, (width - 15).toFloat(), y, paint)
            canvas.drawText(item.quantity.toPlainString(), (width - 190).toFloat(), y, paint)
            canvas.drawText("${item.lineTotal.toPlainString()} ر.ي", 80f, y, paint)
        }

        y += 20f
        canvas.drawLine(10f, y, (width - 10).toFloat(), y, paint)

        y += 32f
        paint.isFakeBoldText = true
        canvas.drawText("المجموع الكلي: ${data.invoice.totalAmount.toPlainString()} ر.ي", (width - 15).toFloat(), y, paint)

        y += 30f
        canvas.drawText("المبلغ المدفوع: ${data.invoice.paidAmount.toPlainString()} ر.ي", (width - 15).toFloat(), y, paint)

        y += 30f
        canvas.drawText("المتبقي (دين): ${data.invoice.remainingAmount.toPlainString()} ر.ي", (width - 15).toFloat(), y, paint)

        y += 45f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 20f
        canvas.drawText("شكراً لتعاملكم معنا", width / 2f, y, paint)

        return bitmap
    }
}
