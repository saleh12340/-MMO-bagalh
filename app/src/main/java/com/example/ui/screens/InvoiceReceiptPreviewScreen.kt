package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.InvoiceWithCustomerAndItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceReceiptPreviewScreen(
    invoiceData: InvoiceWithCustomerAndItems?,
    onBack: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("تفاصيل الفاتورة (إيصال طباعة)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B3B6F))
            )
        },
        bottomBar = {
            Surface(color = Color.White, tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPrint,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة حرارية (58مم)", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة واتساب", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0F4F8))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thermal paper preview container (Screen 5)
            Card(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "بقالة العزي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Text("للمواد الغذائية", fontSize = 13.sp, color = Color.DarkGray)
                    Text("هاتف: 776425052", fontSize = 12.sp, color = Color.DarkGray)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("التاريخ: ${invoiceData?.invoice?.createdAt?.let { "2026/10/20" } ?: "2026/10/20"}", fontSize = 12.sp)
                        Text("رقم الفاتورة: ${invoiceData?.invoice?.invoiceNumber ?: "0021"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("العميل: ${invoiceData?.customer?.name ?: "أحمد محمد علي"}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الصنف", modifier = Modifier.weight(2f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("الكمية", modifier = Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("السعر", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("الإجمالي", modifier = Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Items list
                    val items = invoiceData?.items?.ifEmpty { null } ?: listOf(
                        Triple("أرز فاخر 10 كيلو", "1", "7,500"),
                        Triple("سكر 5 كيلو", "1", "7,010"),
                        Triple("زيت نباتي 1 لتر", "2", "13,620")
                    )

                    items.forEach { item ->
                        val (name, qty, total) = if (item is Triple<*, *, *>) {
                            Triple(item.first.toString(), item.second.toString(), item.third.toString())
                        } else {
                            val ent = item as com.example.data.local.entities.InvoiceItemEntity
                            Triple(ent.productName, ent.quantity.toPlainString(), ent.lineTotal.toPlainString())
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, modifier = Modifier.weight(2f), fontSize = 12.sp)
                            Text(qty, modifier = Modifier.weight(0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text("—", modifier = Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text(total, modifier = Modifier.weight(1.2f), fontSize = 12.sp, textAlign = TextAlign.End, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Summary totals
                    val totalAmt = invoiceData?.invoice?.totalAmount?.toPlainString() ?: "28,130"
                    val paidAmt = invoiceData?.invoice?.paidAmount?.toPlainString() ?: "10,000"
                    val remAmt = invoiceData?.invoice?.remainingAmount?.toPlainString() ?: "18,130"

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المجموع الكلي", fontSize = 13.sp)
                        Text("$totalAmt ر.ي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المبلغ المدفوع", fontSize = 13.sp)
                        Text("$paidAmt ر.ي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المتبقي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        Text("$remAmt ر.ي", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD32F2F))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Signatures (Screen 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("توقيع البائع", fontSize = 11.sp, color = Color.DarkGray)
                            Text("......................", fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("توقيع العميل", fontSize = 11.sp, color = Color.DarkGray)
                            Text("......................", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("شكراً لتعاملكم معنا", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B3B6F))
                }
            }
        }
    }
}
