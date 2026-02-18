package com.example.premove.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.premove.ui.nodes.NodeStatus

@Entity(tableName = "node_runs")
data class NodeRunEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo
    val workflowRunId: String,

    @ColumnInfo
    val nodeId: Int,

    @ColumnInfo
    val status: NodeStatus,

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
