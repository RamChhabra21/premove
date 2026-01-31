package com.example.premove.model

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
)