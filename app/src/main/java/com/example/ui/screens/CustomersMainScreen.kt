package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersMainScreen(
    customers: List<CustomerEntity>,
    totalDebt: BigDecimal?,
    totalCredit: BigDecimal?,
    onCustomerClick: (Long) -> Unit,
    onAddCustomerClick: () -> Unit,
    onNewInvoiceClick: (Long?) -> Unit,
    onAddTransactionClick: (Long?) -> Unit,
    onInventoryClick: () -> Unit,
    onInvoicesClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    selectedBottomTab: Int,
    onBottomTabSelected: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedCustomerId by remember { mutableStateOf<Long?>(null) }

    val filteredCustomers = customers.filter { customer ->
        customer.name.contains(searchQuery.trim(), ignoreCase = true) ||
                (customer.phone?.contains(searchQuery.trim()) == true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "بقالة العزي للمواد الغذائية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Menu, contentDescription = "القائمة", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* trigger search focus */ }) {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B3B6F))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { onBottomTabSelected(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B3B6F), indicatorColor = Color(0xFFE3EDF9))
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { onBottomTabSelected(1) },
                    icon = { Icon(Icons.Default.People, contentDescription = "العملاء") },
                    label = { Text("العملاء", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B3B6F), indicatorColor = Color(0xFFE3EDF9))
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = {
                        onBottomTabSelected(2)
                        onInvoicesClick()
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "الفواتير") },
                    label = { Text("الفواتير", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B3B6F), indicatorColor = Color(0xFFE3EDF9))
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = {
                        onBottomTabSelected(3)
                        onInventoryClick()
                    },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "المخزون") },
                    label = { Text("المخزون", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B3B6F), indicatorColor = Color(0xFFE3EDF9))
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 4,
                    onClick = {
                        onBottomTabSelected(4)
                        onSettingsClick()
                    },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "المزيد") },
                    label = { Text("المزيد", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF1B3B6F), indicatorColor = Color(0xFFE3EDF9))
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
        ) {
            // Search Input Box (Screen 1 style)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("ابحث عن عميل...", fontSize = 14.sp) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFD6DFE8)
                )
            )

            // Customers List (Screen 1 high fidelity rows)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCustomers) { customer ->
                    val isExpanded = expandedCustomerId == customer.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCustomerClick(customer.id) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle & name/phone
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE8EEF5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF1B3B6F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1B2A47)
                                        )
                                        customer.phone?.let {
                                            Text(
                                                text = it,
                                                fontSize = 12.sp,
                                                color = Color(0xFF7A8B9E)
                                            )
                                        }
                                    }
                                }

                                // Balance badge
                                Column(horizontalAlignment = Alignment.End) {
                                    val balance = customer.currentBalance
                                    val isDebtor = balance > BigDecimal.ZERO
                                    val isCreditor = balance < BigDecimal.ZERO
                                    
                                    val statusText = when {
                                        isDebtor -> "مدين"
                                        isCreditor -> "دائن"
                                        else -> "خالص"
                                    }
                                    val amountText = "${balance.abs().toPlainString()} ر.ي"
                                    val statusColor = when {
                                        isDebtor -> Color(0xFFD32F2F) // Red
                                        isCreditor -> Color(0xFF2E7D32) // Green
                                        else -> Color.Gray
                                    }

                                    Text(
                                        text = statusText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = statusColor
                                    )
                                    Text(
                                        text = amountText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        expandedCustomerId = if (isExpanded) null else customer.id
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "توسيع",
                                        tint = Color(0xFF7A8B9E)
                                    )
                                }
                            }

                            // Quick actions when expanded
                            AnimatedVisibility(visible = isExpanded) {
                                Divider(color = Color(0xFFF0F4F8))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    TextButton(onClick = { onCustomerClick(customer.id) }) {
                                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("كشف الحساب", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { onNewInvoiceClick(customer.id) }) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فاتورة", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { onAddTransactionClick(customer.id) }) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("عملية", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
