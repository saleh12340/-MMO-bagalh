package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.ProductEntity
import com.example.utils.toSafeBigDecimal
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateEditScreen(
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    preselectedCustomerId: Long?,
    onBack: () -> Unit,
    onSaveInvoice: (Long, List<Triple<ProductEntity?, String, BigDecimal>>, BigDecimal, BigDecimal, String, String?, String) -> Unit,
    onPrintReceipt: (Long) -> Unit,
    onShareReceipt: (Long) -> Unit
) {
    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == preselectedCustomerId } ?: customers.firstOrNull())
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    val invoiceNumber = remember { "0021" }
    val invoiceDate = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()) }

    var searchProductQuery by remember { mutableStateOf("") }
    var itemQty by remember { mutableStateOf("1") }
    var itemPrice by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val invoiceItems = remember {
        mutableStateListOf(
            Triple(products.find { it.name.contains("أرز") }, "1", BigDecimal("7500")),
            Triple(products.find { it.name.contains("سكر") }, "1", BigDecimal("7010")),
            Triple(products.find { it.name.contains("زيت") }, "2", BigDecimal("6810"))
        )
    }

    var discount by remember { mutableStateOf("0") }
    var paidAmount by remember { mutableStateOf("10000") }
    var notes by remember { mutableStateOf("") }

    val subtotal = invoiceItems.sumOf { (_, qty, price) ->
        val q = qty.toSafeBigDecimal(BigDecimal.ONE)
        price.multiply(q)
    }
    val disc = discount.toSafeBigDecimal(BigDecimal.ZERO)
    val total = subtotal.subtract(disc).max(BigDecimal.ZERO)
    val paid = paidAmount.toSafeBigDecimal(BigDecimal.ZERO)
    val remaining = total.subtract(paid).max(BigDecimal.ZERO)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("إنشاء / تعديل الفاتورة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
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
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // حفظ (Save)
                    Button(
                        onClick = {
                            if (selectedCustomer == null) {
                                errorMessage = "يرجى اختيار العميل أولاً"
                                return@Button
                            }
                            if (invoiceItems.isEmpty()) {
                                errorMessage = "يرجى إضافة أصناف إلى الفاتورة"
                                return@Button
                            }
                            onSaveInvoice(
                                selectedCustomer!!.id,
                                invoiceItems,
                                disc,
                                paid,
                                if (remaining > BigDecimal.ZERO) "آجلة" else "نقدية",
                                notes.ifBlank { null },
                                invoiceNumber
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }

                    // طباعة (Print)
                    Button(
                        onClick = { onPrintReceipt(1L) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة", fontWeight = FontWeight.Bold)
                    }

                    // مشاركة (Share)
                    Button(
                        onClick = { onShareReceipt(1L) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3B6F)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontWeight = FontWeight.Bold)
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            errorMessage?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                }
            }

            // Customer Selector (Screen 4)
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "اختر العميل",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("اختر العميل") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1B3B6F)) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text(customer.name) },
                            onClick = {
                                selectedCustomer = customer
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Invoice Number & Date row (Screen 4)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رقم الفاتورة", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B2A47))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("التاريخ", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(invoiceDate, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B2A47))
                    }
                }
            }

            // Search product field
            OutlinedTextField(
                value = searchProductQuery,
                onValueChange = { query ->
                    searchProductQuery = query
                    val found = products.find { it.name.contains(query.trim(), true) || it.barcode == query.trim() }
                    if (found != null && itemPrice.isBlank()) {
                        itemPrice = found.salePrice.toPlainString()
                    }
                },
                placeholder = { Text("ابحث عن صنف...", fontSize = 13.sp) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            // Table Header (Screen 4 style: الصنف | الكمية | سعر الوحدة | الإجمالي)
            Surface(
                color = Color(0xFF1B3B6F),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(28.dp))
                    Text("الصنف", modifier = Modifier.weight(2f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("الكمية", modifier = Modifier.weight(1f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("سعر الوحدة", modifier = Modifier.weight(1.2f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text("الإجمالي", modifier = Modifier.weight(1.2f), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                }
            }

            // Table Rows
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(invoiceItems) { item ->
                    val (product, qty, price) = item
                    val lineTotal = price.multiply(qty.toSafeBigDecimal(BigDecimal.ONE))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE))
                                    .clickable { invoiceItems.remove(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(product?.name ?: "صنف", modifier = Modifier.weight(2f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(qty, modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(price.toPlainString(), modifier = Modifier.weight(1.2f), fontSize = 12.sp, textAlign = TextAlign.Center)
                            Text(lineTotal.toPlainString(), modifier = Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                    }
                }
            }

            // Green "+ إضافة الصنف" button
            Button(
                onClick = {
                    val p = products.find { it.name.contains(searchProductQuery.trim(), true) } ?: products.firstOrNull()
                    val pr = itemPrice.toSafeBigDecimal(p?.salePrice ?: BigDecimal("1000"))
                    val qt = itemQty.toSafeBigDecimal(BigDecimal.ONE)
                    invoiceItems.add(Triple(p, qt.toPlainString(), pr))
                    searchProductQuery = ""
                    itemPrice = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة الصنف", fontWeight = FontWeight.Bold)
            }

            // Totals Summary Box (Screen 4)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المجموع الكلي", fontSize = 13.sp, color = Color.Gray)
                        Text("${total.toPlainString()} ر.ي", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("المبلغ المدفوع", fontSize = 13.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = paidAmount,
                                onValueChange = { paidAmount = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(110.dp).height(48.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ر.ي", fontSize = 12.sp)
                        }
                    }
                    Divider(color = Color(0xFFF0F4F8))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المتبقي", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        Text("${remaining.toPlainString()} ر.ي", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}
