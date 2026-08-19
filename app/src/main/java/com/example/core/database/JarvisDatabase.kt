package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        TaskEntity::class,
        MemoryEntity::class,
        RoutineEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun taskDao(): TaskDao
    abstract fun memoryDao(): MemoryDao
    abstract fun routineDao(): RoutineDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core_db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default memories & routines
                        CoroutineScope(Dispatchers.IO).launch {
                            val routineDao = getDatabase(context).routineDao()
                            val memoryDao = getDatabase(context).memoryDao()

                            // Seed default routines
                            routineDao.insertRoutine(
                                RoutineEntity(
                                    triggerPhrase = "good morning",
                                    name = "Morning Briefing",
                                    description = "Reports time, battery, reads notifications & briefs for the day",
                                    stepsJson = "DEVICE_INFO|READ_NOTIFICATIONS|AI_CONVERSATION",
                                    isEnabled = true,
                                    iconName = "wb_sunny"
                                )
                            )
                            routineDao.insertRoutine(
                                RoutineEntity(
                                    triggerPhrase = "study mode",
                                    name = "Study & Focus Protocol",
                                    description = "Sets volume to 40%, enables timer, creates study reminder",
                                    stepsJson = "CONTROL_VOLUME|SET_TIMER|CREATE_REMINDER",
                                    isEnabled = true,
                                    iconName = "school"
                                )
                            )
                            routineDao.insertRoutine(
                                RoutineEntity(
                                    triggerPhrase = "gaming mode",
                                    name = "Gaming Setup",
                                    description = "Launches YouTube, checks battery level, sets high volume",
                                    stepsJson = "DEVICE_INFO|CONTROL_VOLUME|OPEN_APP",
                                    isEnabled = true,
                                    iconName = "sports_esports"
                                )
                            )

                            // Seed default preferences
                            memoryDao.insertMemory(
                                MemoryEntity(
                                    category = "PREFERENCE",
                                    key = "Response Style",
                                    value = "Concise and futuristic JARVIS persona"
                                )
                            )
                            memoryDao.insertMemory(
                                MemoryEntity(
                                    category = "PREFERENCE",
                                    key = "Preferred Assistant Name",
                                    value = "JARVIS"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
