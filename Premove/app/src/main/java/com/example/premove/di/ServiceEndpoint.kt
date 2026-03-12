package com.example.premove.di

import com.example.premove.data.repository.EdgeRepository
import com.example.premove.data.repository.EdgeRunRepository
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
import com.example.premove.data.repository.WorkflowRunRepository
import com.example.premove.network.LlmClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPoint {
    fun workflowRepository(): WorkflowRepository
    fun workflowRunRepository(): WorkflowRunRepository
    fun nodeRepository(): NodeRepository
    fun nodeRunRepository(): NodeRunRepository
    fun edgeRepository(): EdgeRepository
    fun edgeRunRepository(): EdgeRunRepository
    fun llmClient(): LlmClient
}