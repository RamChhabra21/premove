package com.example.premove.data.local.dao

import androidx.room.*
import com.example.premove.data.local.entity.EdgeRunEntity

@Dao
interface EdgeRunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(edgeRun: EdgeRunEntity)

    @Query("SELECT * FROM edge_runs WHERE workflowRunId = :workflowRunId")
    suspend fun getByWorkflowRunId(workflowRunId: String): List<EdgeRunEntity>
}