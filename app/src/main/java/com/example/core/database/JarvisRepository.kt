package com.example.core.database

import com.example.core.model.MemoryCategory
import com.example.core.model.MemoryItem
import com.example.core.model.RoutineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JarvisRepository(private val database: JarvisDatabase) {

    val allConversations: Flow<List<ConversationEntity>> =
        database.conversationDao().getAllConversations()

    val allTasks: Flow<List<TaskEntity>> =
        database.taskDao().getAllTasks()

    val allMemories: Flow<List<MemoryItem>> =
        database.memoryDao().getAllMemories().map { list ->
            list.map {
                MemoryItem(
                    id = it.id,
                    category = try {
                        MemoryCategory.valueOf(it.category)
                    } catch (e: Exception) {
                        MemoryCategory.CUSTOM
                    },
                    key = it.key,
                    value = it.value,
                    timestamp = it.timestamp
                )
            }
        }

    val allRoutines: Flow<List<RoutineItem>> =
        database.routineDao().getAllRoutines().map { list ->
            list.map {
                RoutineItem(
                    id = it.id,
                    triggerPhrase = it.triggerPhrase,
                    name = it.name,
                    description = it.description,
                    stepsJson = it.stepsJson,
                    isEnabled = it.isEnabled,
                    iconName = it.iconName
                )
            }
        }

    val allNotes: Flow<List<NoteEntity>> =
        database.noteDao().getAllNotes()

    suspend fun insertConversation(userQuery: String, response: String, intent: String = "", latencyMs: Long = 0L) {
        database.conversationDao().insertConversation(
            ConversationEntity(
                userQuery = userQuery,
                assistantResponse = response,
                intent = intent,
                latencyMs = latencyMs
            )
        )
    }

    suspend fun clearHistory() {
        database.conversationDao().clearAllConversations()
        database.taskDao().clearAllTasks()
    }

    suspend fun insertTask(task: TaskEntity) {
        database.taskDao().insertTask(task)
    }

    suspend fun insertMemory(category: MemoryCategory, key: String, value: String) {
        database.memoryDao().insertMemory(
            MemoryEntity(
                category = category.name,
                key = key,
                value = value
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        database.memoryDao().deleteMemoryById(id)
    }

    suspend fun clearMemories() {
        database.memoryDao().clearAllMemories()
    }

    suspend fun insertRoutine(routine: RoutineItem) {
        database.routineDao().insertRoutine(
            RoutineEntity(
                triggerPhrase = routine.triggerPhrase,
                name = routine.name,
                description = routine.description,
                stepsJson = routine.stepsJson,
                isEnabled = routine.isEnabled,
                iconName = routine.iconName
            )
        )
    }

    suspend fun updateRoutine(routine: RoutineItem) {
        database.routineDao().updateRoutine(
            RoutineEntity(
                id = routine.id,
                triggerPhrase = routine.triggerPhrase,
                name = routine.name,
                description = routine.description,
                stepsJson = routine.stepsJson,
                isEnabled = routine.isEnabled,
                iconName = routine.iconName
            )
        )
    }

    suspend fun deleteRoutine(id: Long) {
        database.routineDao().deleteRoutineById(id)
    }

    suspend fun getActiveRoutines(): List<RoutineEntity> {
        return database.routineDao().getActiveRoutines()
    }

    suspend fun insertNote(title: String, content: String) {
        database.noteDao().insertNote(
            NoteEntity(title = title, content = content)
        )
    }

    suspend fun deleteNote(id: Long) {
        database.noteDao().deleteNoteById(id)
    }
}
