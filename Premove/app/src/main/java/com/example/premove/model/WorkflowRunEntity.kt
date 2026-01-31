package com.example.premove.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class WorkflowRunEntity(
    @PrimaryKey(autoGenerate = true)
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
