package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val actionType: String, // CREATE, UPDATE, DELETE, CANCEL
    val entityName: String,
    val entityId: String,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)
