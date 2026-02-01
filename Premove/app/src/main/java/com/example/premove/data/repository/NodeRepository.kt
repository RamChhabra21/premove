package com.example.premove.data.repository

import android.content.Context
import com.example.premove.data.local.AppDatabase
import com.example.premove.data.local.NodeDao
import com.example.premove.model.NodeEntity
import javax.inject.Inject

class NodeRepository @Inject constructor(
    private val dao: NodeDao
) {

    fun getAllNodes() = dao.getAllNodes()

    suspend fun getNodeById(nodeId: String) {
        dao.getNodeById(nodeId)
    }

    suspend fun insertNode(node: NodeEntity) {
        dao.insertNode(node)
    }

    suspend fun updateNode(node: NodeEntity){
        dao.updateNode(node)
    }

    suspend fun deleteNode(nodeId: String){
        dao.deleteNodeById(nodeId = nodeId)
    }
}

