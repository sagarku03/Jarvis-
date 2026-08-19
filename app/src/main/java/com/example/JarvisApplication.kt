package com.example

import android.app.Application
import com.example.core.automation.CommunicationController
import com.example.core.automation.DeviceController
import com.example.core.database.JarvisDatabase
import com.example.core.database.JarvisRepository

class JarvisApplication : Application() {

    lateinit var database: JarvisDatabase
        private set

    lateinit var repository: JarvisRepository
        private set

    lateinit var deviceController: DeviceController
        private set

    lateinit var commsController: CommunicationController
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = JarvisDatabase.getDatabase(this)
        repository = JarvisRepository(database)
        deviceController = DeviceController(this)
        commsController = CommunicationController(this)
    }

    companion object {
        lateinit var instance: JarvisApplication
            private set
    }
}
