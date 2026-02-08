package com.example.premove.data.repository

import com.example.premove.data.local.dao.EdgeDao
import com.example.premove.data.local.dao.NodeDao
import com.example.premove.data.local.entity.EdgeEntity
import com.example.premove.data.local.entity.NodeEntity
import javax.inject.Inject

class EdgeRepository @Inject constructor(
    private val dao: EdgeDao
) {
    suspend fun insertEdge(edge: EdgeEntity) {
        dao.insertEdge(edge)
    }
}