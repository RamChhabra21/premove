package com.example.premove.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo
    val title: String,
    @ColumnInfo
    val description: String,
    @ColumnInfo
    val isEnabled: Boolean,
    @ColumnInfo
    val createdBy: Int
)
