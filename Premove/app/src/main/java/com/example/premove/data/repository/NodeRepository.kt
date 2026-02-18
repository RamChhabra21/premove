package com.example.premove.data.repository

import androidx.compose.ui.geometry.Offset
import com.example.premove.data.local.dao.NodeDao
import com.example.premove.data.local.entity.NodeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NodeRepository @Inject constructor(
    private val dao: NodeDao
) {

    fun getAllNodes() = dao.getAllNodes()

    suspend fun getNodeById(nodeId: Int): NodeEntity? {
        return dao.getNodeById(nodeId)
    }

    fun observeNodesByWorkflowId(workflowId: String) :  Flow<List<NodeEntity>> {
        return dao.observeNodesByWorkflowId(workflowId)
    }

    suspend fun getNodesByWorkflowId(workflowId: String) :  List<NodeEntity> {
        return dao.getNodesByWorkflowId(workflowId)
    }

    suspend fun getNodesToBeInitialised(workflowId: String): List<NodeEntity>{
        return dao.getNodesToBeInitialised(workflowId)
    }

    suspend fun insertNode(node: NodeEntity) {
        dao.insertNode(node)
    }

    suspend fun updateNode(node: NodeEntity){
        dao.updateNode(node)
    }

    suspend fun updateNodeConfig(nodeId: Int, title: String, type: String, configJson: String){
        dao.updateNodeConfig(nodeId, title, type, configJson)
    }

    // update node position
    suspend fun updateNodePosition(nodeId: Int, newPos : Offset){
        dao.updateNodePosition(nodeId, newPos.x, newPos.y)
    }

    suspend fun deleteNode(nodeId: Int){
        dao.deleteNodeById(nodeId = nodeId)
    }
}

