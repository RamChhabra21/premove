package com.example.premove.engine

sealed class NodeExecutionResult {
    data class Completed(val output: String) : NodeExecutionResult()
    data class Pending(val jobId: String) : NodeExecutionResult()
    data class Failed(val error: String) : NodeExecutionResult()
}