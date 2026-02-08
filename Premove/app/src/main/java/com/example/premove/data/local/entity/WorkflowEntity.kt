package com.example.premove.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey()
    val id: String,

    @ColumnInfo
    val title: String,

    @ColumnInfo
    val description: String,

    @ColumnInfo
    val isEnabled: Boolean,

    @ColumnInfo
    val createdBy: Int,

    @ColumnInfo
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo
    val updatedAt: Long = System.currentTimeMillis()
)
