package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CustomerEntity
import com.example.utils.toSafeBigDecimal
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    customers: List<CustomerEntity>,
    preselectedCustomerId: Long?,
    onBack: () -> Unit,
    onSaveTransaction: (Long, BigDecimal, Boolean, String, String?) -> Unit,
    onShare: (String) -> Unit
) {
    var selectedCustomer by remember {
        mutableStateOf(customers.find { it.id == preselectedCustomerId } ?: customers.firstOrNull())
    }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDebtor by remember { mutableStateOf(false) } // false = "لنا" (creditor/payment), true = "عليه" (debtor/charge)
    var transactionType by remember { mutableStateOf("سداد نقدي") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentDateStr = remember {
        SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
    }

    val transactionTypes = listOf("سداد نقدي", "بيع آجل", "دفعة جزئية", "دفعة كاملة", "خصم مالي", "تسوية حساب")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("إضافة عملية جديدة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        val txt = "عملية مالية: $transactionType بقيمة $amount ر.ي للعميل ${selectedCustomer?.name ?: ""}"
                        onShare(txt)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF1B3B6F))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة", color = Color(0xFF1B3B6F), fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = {
                        // Copy/duplicate
                        description = "$description (نسخة)"
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1B3B6F))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ", color = Color(0xFF1B3B6F), fontWeight = FontWeight.Bold)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            errorMessage?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                    }
                }
            }

            // Customer Selector
            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCustomer?.name ?: "اختر العميل",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("العميل") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
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

            // Amount Input Card (Screen 3 style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("المبلغ", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it
                                errorMessage = null
                            },
                            placeholder = { Text("0.00", fontSize = 20.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFD6DFE8),
                                focusedBorderColor = Color(0xFF1B3B6F)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8EEF5),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
                                Text("ر.ي", fontWeight = FontWeight.Bold, color = Color(0xFF1B3B6F))
                            }
                        }
                    }
                }
            }

            // Date
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF1B3B6F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("التاريخ", fontSize = 13.sp, color = Color.Gray)
                    }
                    Text(currentDateStr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Transaction Details
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("تفاصيل العملية") },
                placeholder = { Text("مثال: سداد جزء من الدين") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            // Direction Toggle ("لنا" vs "عليه")
            Column {
                Text("الإتجاه", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isDebtor = false
                            transactionType = "سداد نقدي"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isDebtor) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
                            contentColor = if (!isDebtor) Color.White else Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("لنا", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            isDebtor = true
                            transactionType = "بيع آجل"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDebtor) Color(0xFFD32F2F) else Color(0xFFFFEBEE),
                            contentColor = if (isDebtor) Color.White else Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("عليه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Transaction Type Selector
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = transactionType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع العملية") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    transactionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                transactionType = type
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button (Screen 3 style)
            Button(
                onClick = {
                    val parsed = amount.toSafeBigDecimal(BigDecimal.ZERO)
                    if (selectedCustomer == null) {
                        errorMessage = "يرجى اختيار العميل أولاً"
                        return@Button
                    }
                    if (parsed.compareTo(BigDecimal.ZERO) <= 0) {
                        errorMessage = "يرجى إدخال مبلغ صحيح للعملية"
                        return@Button
                    }
                    onSaveTransaction(selectedCustomer!!.id, parsed, isDebtor, transactionType, description.ifBlank { null })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ العملية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
