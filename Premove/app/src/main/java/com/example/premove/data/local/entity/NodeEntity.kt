package com.example.premove.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.premove.domain.model.NodeLayoutType

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo
    val workflowId: String,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val type: String,

    @ColumnInfo
    val x: Float,

    @ColumnInfo
    val y: Float,

    @ColumnInfo
    val layoutType: NodeLayoutType,

    @ColumnInfo
    val configJson: String? = null  // node‑specific config (URL, delay, etc.)
)