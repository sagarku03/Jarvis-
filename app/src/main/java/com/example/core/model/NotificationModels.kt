package com.example.core.model

data class CapturedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long = System.currentTimeMillis(),
    var isRead: Boolean = false
)
