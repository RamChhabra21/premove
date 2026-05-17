package com.example.premove.domain.model

enum class NodeCategory {
    TRIGGER, // Passive listener, fires the flow
    ACTION,  // Executor or Agent
    WAIT     // Delays and pauses
}
