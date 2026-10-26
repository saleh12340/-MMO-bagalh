package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    totalSales: BigDecimal?,
    totalCollected: BigDecimal?,
    totalDebt: BigDecimal?,
    totalCredit: BigDecimal?,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("الكل") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("التقارير / السجل", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.BarChart, contentDescription = "رسم بياني", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B3B6F))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Date Filter Card (Screen 7: من 2026/10/01 إلى 2026/10/23)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("من 2026/10/01", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إلى 2026/10/23", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Tabs Row (Screen 7: الكل | الفواتير | المدفوعات | الديون)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("الكل", "الفواتير", "المدفوعات", "الديون").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Button(
                        onClick = { selectedTab = tab },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF1B3B6F) else Color.White,
                            contentColor = if (isSelected) Color.White else Color(0xFF1B3B6F)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        elevation = ButtonDefaults.buttonElevation(if (isSelected) 2.dp else 0.dp)
                    ) {
                        Text(tab, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Summary Cards (Screen 7 items)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == "الكل" || selectedTab == "الفواتير") {
                    item {
                        ReportDetailedCard(
                            title = "الفواتير",
                            countText = "عدد الفواتير: 12",
                            amountText = "${totalSales?.toPlainString() ?: "215,500"} ر.ي",
                            amountColor = Color(0xFF2E7D32),
                            icon = Icons.Default.ReceiptLong
                        )
                    }
                }

                if (selectedTab == "الكل" || selectedTab == "المدفوعات") {
                    item {
                        ReportDetailedCard(
                            title = "المدفوعات",
                            countText = "عدد المدفوعات: 8",
                            amountText = "${totalCollected?.toPlainString() ?: "68,000"} ر.ي",
                            amountColor = Color(0xFF2E7D32),
                            icon = Icons.Default.AccountBalanceWallet
                        )
                    }
                }

                if (selectedTab == "الكل" || selectedTab == "الديون") {
                    item {
                        ReportDetailedCard(
                            title = "الديون",
                            countText = "عدد الديون: 6",
                            amountText = "${totalDebt?.toPlainString() ?: "182,500"} ر.ي",
                            amountColor = Color(0xFFD32F2F),
                            icon = Icons.Default.WarningAmber
                        )
                    }
                }

                if (selectedTab == "الكل") {
                    item {
                        ReportDetailedCard(
                            title = "كشف حساب العملاء",
                            countText = "إجمالي الرصيد",
                            amountText = "125,000 ر.ي",
                            amountColor = Color(0xFFD32F2F),
                            icon = Icons.Default.Group
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDetailedCard(
    title: String,
    countText: String,
    amountText: String,
    amountColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B2A47))
                    Text(countText, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(amountText, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = amountColor)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}
