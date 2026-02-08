package com.example.premove.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class NodeRunEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo
    val workflowRunId: String,

    @ColumnInfo
    val nodeId: String,

    @ColumnInfo
    val status: String,

    @ColumnInfo
    val inputData: String? = null,

    @ColumnInfo
    val outputData: String? = null,

    @ColumnInfo
    val error: String? = null,

    @ColumnInfo
    val startedAt: Long = System.currentTimeMillis(),

    @ColumnInfo
    val endedAt: Long = System.currentTimeMillis()
)
