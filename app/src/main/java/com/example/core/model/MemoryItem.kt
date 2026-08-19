package com.example.core.model

enum class MemoryCategory {
    PREFERENCE,
    CONTACT,
    APP,
    FACT,
    CUSTOM
}

data class MemoryItem(
    val id: Long = 0,
    val category: MemoryCategory,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
