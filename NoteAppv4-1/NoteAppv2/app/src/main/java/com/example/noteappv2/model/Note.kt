package com.example.noteappv2.model


enum class Importance {
    LOW, MEDIUM, HIGH, URGENT
}


data class Note(
    val id: Int = 0,
    val title: String = "Untitled Note",
    val content: String = "No content available",
    val importance: Importance = Importance.MEDIUM,
    val timestamp: Long = System.currentTimeMillis()
)
