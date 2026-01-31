package com.example.premove.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: String,

    @ColumnInfo
    val workflowId: Int,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val type: String,

    @ColumnInfo
    val x: Float,

    @ColumnInfo
    val y: Float,

    @ColumnInfo
    val configJson: String? = null  // node‑specific config (URL, delay, etc.)
)