package com.example.premove.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EdgeEntity (
    @PrimaryKey
    var id: String,

    @ColumnInfo
    var workflowId: String,

    @ColumnInfo
    var sourceNodeId: String,

    @ColumnInfo
    var targetNodeId: String,

    @ColumnInfo
    val bendX: Float = 0f,  // default 0 = handle at midpoint

    @ColumnInfo
    val bendY: Float = 0f   // default 0 = handle at midpoint
)