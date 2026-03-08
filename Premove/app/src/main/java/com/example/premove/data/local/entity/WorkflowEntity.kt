package com.example.premove.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val description: String,

    @ColumnInfo
    val isEnabled: Boolean,

    @ColumnInfo
    val createdBy: Int,

    // ── Workflow config ───────────────────────────────────────────────────────
    @ColumnInfo
    val triggerType: String = "MANUAL",      // MANUAL | SCHEDULED | WEBHOOK | EVENT

    @ColumnInfo
    val cronExpression: String = "0 9 * * 1-5",

    @ColumnInfo
    val webhookSecret: String = "",

    @ColumnInfo
    val timeoutMinutes: Int = 5,

    @ColumnInfo
    val maxRetries: Int = 0,

    @ColumnInfo
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo
    val updatedAt: Long = System.currentTimeMillis()
)