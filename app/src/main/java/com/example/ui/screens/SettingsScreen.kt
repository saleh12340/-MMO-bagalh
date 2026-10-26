package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onStoreProfileClick: () -> Unit = {},
    onBluetoothPrinterClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {}
) {
    var showDialogTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("الإعدادات", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B3B6F))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7F9FC))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Group 1: البيانات الأساسية
            item {
                SettingsGroup(title = "البيانات الأساسية") {
                    SettingsRowItem(
                        icon = Icons.Default.Storefront,
                        title = "تعريف المتجر",
                        subtitle = "بقالة العزي للمواد الغذائية",
                        onClick = { showDialogTitle = "تعريف المتجر: بقالة العزي للمواد الغذائية - هاتف: 776425052" }
                    )
                    Divider(color = Color(0xFFF0F4F8))
                    SettingsRowItem(
                        icon = Icons.Default.AttachMoney,
                        title = "العملة",
                        statusText = "ريال يمني",
                        onClick = { showDialogTitle = "العملة المعتمدة: ريال يمني (ر.ي)" }
                    )
                    Divider(color = Color(0xFFF0F4F8))
                    SettingsRowItem(
                        icon = Icons.Default.Description,
                        title = "تنسيق الفاتورة",
                        onClick = { showDialogTitle = "تنسيق الفاتورة: إيصال حراري 58مم بنظام الباركود والرقم التسلسلي" }
                    )
                }
            }

            // Group 2: الطباعة والمشاركة
            item {
                SettingsGroup(title = "الطباعة والمشاركة") {
                    SettingsRowItem(
                        icon = Icons.Default.Print,
                        title = "طابعة بلوتوث (58 مم)",
                        statusText = "متصل",
                        statusColor = Color(0xFF2E7D32),
                        onClick = onBluetoothPrinterClick
                    )
                    Divider(color = Color(0xFFF0F4F8))
                    SettingsRowItem(
                        icon = Icons.Default.Share,
                        title = "مشاركة عبر واتساب",
                        statusText = "مفعل",
                        statusColor = Color(0xFF2E7D32),
                        onClick = { showDialogTitle = "المشاركة عبر واتساب مفعلة تلقائياً لإرسال صور الإيصالات والنصوص" }
                    )
                }
            }

            // Group 3: النسخ الاحتياطي
            item {
                SettingsGroup(title = "النسخ الاحتياطي") {
                    SettingsRowItem(
                        icon = Icons.Default.CloudUpload,
                        title = "النسخ الاحتياطي",
                        onClick = onBackupClick
                    )
                    Divider(color = Color(0xFFF0F4F8))
                    SettingsRowItem(
                        icon = Icons.Default.Restore,
                        title = "استعادة البيانات",
                        onClick = onRestoreClick
                    )
                }
            }

            // Group 4: المظهر والأمان
            item {
                SettingsGroup(title = "المظهر والأمان") {
                    SettingsRowItem(
                        icon = Icons.Default.FontDownload,
                        title = "الخط والتصميم",
                        statusText = "أميري",
                        onClick = { showDialogTitle = "نوع الخط: خط أميري عربي عريض للأرقام والعناوين" }
                    )
                    Divider(color = Color(0xFFF0F4F8))
                    SettingsRowItem(
                        icon = Icons.Default.Lock,
                        title = "كلمة المرور",
                        onClick = { showDialogTitle = "قفل التطبيق وحماية الحسابات بكلمة مرور" }
                    )
                }
            }
        }
    }

    showDialogTitle?.let { message ->
        AlertDialog(
            onDismissRequest = { showDialogTitle = null },
            title = { Text("معلومات الإعدادات") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { showDialogTitle = null }) {
                    Text("حسناً")
                }
            }
        )
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7A8B9E),
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color = Color.Gray,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1B3B6F), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1B2A47))
                subtitle?.let {
                    Text(it, fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            statusText?.let {
                Text(it, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}
