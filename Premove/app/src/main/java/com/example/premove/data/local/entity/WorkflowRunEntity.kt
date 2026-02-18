package com.example.premove.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflow_runs")
data class WorkflowRunEntity(
    @PrimaryKey()
    val id: String,

    @ColumnInfo
    val workflowId: String,

    @ColumnInfo
    val status: String,

    @ColumnInfo
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo
    val updatedAt: Long = System.currentTimeMillis()
)
