package com.example.premove.engine

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity

interface NodeExecutionStrategy {
    suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult
}