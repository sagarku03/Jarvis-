package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.MemoryCategory

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userQuery: String,
    val assistantResponse: String,
    val intent: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val latencyMs: Long = 0L
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val userQuery: String,
    val intent: String,
    val stepsSummary: String,
    val status: String,
    val startTime: Long,
    val endTime: Long?,
    val resultSummary: String
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // PREFERENCE, CONTACT, APP, FACT, CUSTOM
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val triggerPhrase: String,
    val name: String,
    val description: String,
    val stepsJson: String,
    val isEnabled: Boolean = true,
    val iconName: String = "routine"
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
