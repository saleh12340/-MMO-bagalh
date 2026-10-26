package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.data.local.entities.InvoiceEntity
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: CustomerEntity?,
    transactions: List<FinancialTransactionEntity>,
    invoices: List<InvoiceEntity>,
    onBack: () -> Unit,
    onNewInvoice: () -> Unit,
    onAddPayment: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditCustomer: () -> Unit,
    onDeleteCustomer: () -> Unit,
    onShareStatement: () -> Unit,
    onInvoiceClick: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) } // 0: الفواتير, 1: العمليات
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val balance = customer.currentBalance
    val isDebtor = balance > BigDecimal.ZERO
    val isCreditor = balance < BigDecimal.ZERO
    val balanceColor = when {
        isDebtor -> Color(0xFFD32F2F)
        isCreditor -> Color(0xFF2E7D32)
        else -> Color.Gray
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("حساب العميل", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditCustomer) {
                        Icon(Icons.Default.MoreVert, contentDescription = "خيارات", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B3B6F))
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNewInvoice() }
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(20.dp))
                        Text("إنشاء فاتورة", fontSize = 11.sp, color = Color(0xFF1B3B6F))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onEditCustomer() }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(20.dp))
                        Text("تعديل", fontSize = 11.sp, color = Color(0xFF1B3B6F))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showDeleteConfirm = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                        Text("حذف", fontSize = 11.sp, color = Color(0xFFD32F2F))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onShareStatement() }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(20.dp))
                        Text("مشاركة", fontSize = 11.sp, color = Color(0xFF1B3B6F))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNewInvoice() }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(20.dp))
                        Text("نسخ", fontSize = 11.sp, color = Color(0xFF1B3B6F))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
        ) {
            // Profile Card (Screen 2)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EEF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B2A47))
                    customer.phone?.let {
                        Text(it, fontSize = 13.sp, color = Color(0xFF7A8B9E))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        if (isDebtor) "مدين" else if (isCreditor) "دائن" else "خالص",
                        fontSize = 12.sp,
                        color = balanceColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${balance.abs().toPlainString()} ر.ي",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                    Text("الرصيد الحالي", fontSize = 11.sp, color = Color(0xFF7A8B9E))
                }
            }

            // Segmented Tab Control (الفواتير | العمليات)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) Color(0xFF1B3B6F) else Color(0xFFE8EEF5),
                        contentColor = if (selectedTab == 0) Color.White else Color(0xFF1B3B6F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("الفواتير", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) Color(0xFF1B3B6F) else Color(0xFFE8EEF5),
                        contentColor = if (selectedTab == 1) Color.White else Color(0xFF1B3B6F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("العمليات", fontWeight = FontWeight.Bold)
                }
            }

            // Two main action buttons (+ إضافة دفعة | + إضافة عملية)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddPayment,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة دفعة", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddTransaction,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة عملية", fontWeight = FontWeight.Bold)
                }
            }

            // List of Invoices or Transactions
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (selectedTab == 0) {
                    // Invoices list
                    items(invoices) { inv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInvoiceClick(inv.id) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = Color(0xFF1B3B6F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("فاتورة رقم ${inv.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(sdf.format(Date(inv.createdAt)), fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${inv.totalAmount.toPlainString()} ر.ي",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp
                                    )
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                } else {
                    // Transactions list (Screen 2 rows)
                    items(transactions) { tx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isPayment = tx.direction.contains("لنا") || tx.transactionType.contains("دفع") || tx.transactionType.contains("سداد")
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isPayment) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (isPayment) "$" else "📄",
                                            fontSize = 14.sp,
                                            color = if (isPayment) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(tx.transactionType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(sdf.format(Date(tx.createdAt)), fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isPositive = tx.direction.contains("لنا") || tx.transactionType.contains("دفع") || tx.transactionType.contains("سداد")
                                    Text(
                                        "${tx.amount.toPlainString()} ر.ي",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                        fontSize = 14.sp
                                    )
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف حساب العميل ${customer.name}؟ لن يمكن التراجع عن هذه العملية.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteCustomer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}
