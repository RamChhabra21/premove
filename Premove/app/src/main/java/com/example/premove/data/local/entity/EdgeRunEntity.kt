package com.example.premove.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edge_runs")
data class EdgeRunEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo
    val workflowRunId: String,

    @ColumnInfo
    val edgeId: String,

    @ColumnInfo
    val didPropagate: Boolean,       // did LLM decide to flow through this edge?

    @ColumnInfo
    val llmReasoning: String? = null, // why LLM decided yes/no, useful for debugging

    @ColumnInfo
    val evaluatedAt: Long = System.currentTimeMillis()
)