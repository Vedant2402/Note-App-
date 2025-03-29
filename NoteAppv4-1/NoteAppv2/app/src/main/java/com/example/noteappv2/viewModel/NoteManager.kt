package com.example.noteappv2.viewModel

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import com.example.noteappv2.model.Importance
import com.example.noteappv2.model.Note
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.Instant

object NoteManager {

    private val notes = mutableListOf<Note>()
    val db = Firebase.firestore
    val databaseReference = db.collection("notes")


    fun addNote(title: String, content: String, importance: Importance = Importance.MEDIUM) {
        val id = if (notes.isEmpty()) 1 else notes.maxOf { it.id } + 1
        val timestamp = System.currentTimeMillis()
        val newNote = Note(id, title, content, importance, timestamp)
        notes.add(newNote)

        databaseReference.document(newNote.id.toString())
            .set(newNote)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.e(TAG,"Error writing note: $e")
            }
    }

    fun fetchNotes(onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseReference
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    try {
                        val note = Note(
                            id = (document["id"] as Long).toInt(),
                            title = document["title"] as String,
                            content = document["content"] as String,
                            importance = Importance.valueOf(document["importance"] as String),
                            timestamp = document["timestamp"] as Long
                        )
                        notes.add(note)
                        Log.d(TAG, "$note")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting document to Note", e)
                    }
                }
                onSuccess(notes)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                onFailure(exception)
            }

    }
    // Getting realtime update with cloud Firestore (https://firebase.google.com/docs/firestore/query-data/listen#kotlin+ktx)
    fun fetchNotesRealTime(onUpdate: (List<Note>) -> Unit, onFailure: (Exception) -> Unit) {
        databaseReference
            .orderBy("timestamp")
            .whereEqualTo("importance", "URGENT")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed", e)
                    onFailure(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val updatedNotes = mutableListOf<Note>()

                    for (document in snapshot.documents) {
                        try {
                            val note = Note(
                                id = (document["id"] as? Long)?.toInt() ?: 0,
                                title = (document["title"] as? String) ?: "Untitled Note",
                                content = (document["content"] as? String) ?: "No content available",
                                importance = Importance.valueOf((document["importance"] as? String) ?: "MEDIUM"),
                                timestamp = (document["timestamp"] as? Long) ?: System.currentTimeMillis()
                            )
                            updatedNotes.add(note)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting document to Note", e)
                        }
                    }

                    notes.clear()
                    updatedNotes.sortByDescending { it.timestamp }
                    notes.addAll(updatedNotes)

                    onUpdate(updatedNotes)
                }
            }
    }




    fun deleteNote(noteId: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        databaseReference.document(noteId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                notes.removeIf { it.id == noteId } 
                onSuccess() 
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting document", e)
                onFailure(e)
            }
    }


    fun updateNote(
        noteId: Int,
        title: String,
        content: String,
        importance: Importance,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updatedNote = Note(
            id = noteId,
            title = title,
            content = content,
            importance = importance,
            timestamp = System.currentTimeMillis()
        )

        databaseReference.document(noteId.toString())
            .set(updatedNote)
            .addOnSuccessListener {
                notes.replaceAll { if (it.id == noteId) updatedNote else it } 
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


    fun getAllNotes(): List<Note> = notes


    // Sample data for demonstration purposes
    @SuppressLint("NewApi")
    fun getSampleNotes(): List<Note> {
        return listOf(
            Note(
                id = 1,
                title = "Grocery List",
                content = "Buy milk, eggs, bread, and coffee.",
                importance = Importance.MEDIUM,
                timestamp = Instant.now().toEpochMilli()
            ),
            Note(
                id = 2,
                title = "Meeting Notes",
                content = "Discuss project timeline and assign tasks.",
                importance = Importance.URGENT,
                timestamp = Instant.now().toEpochMilli()
            ),
            Note(
                id = 3,
                title = "To-Do List",
                content = "Finish Kotlin course, clean the house, and go to the gym.",
                importance = Importance.MEDIUM,
                timestamp = Instant.now().toEpochMilli()
            ),
            Note(
                id = 4,
                title = "Meeting Reminder",
                content = "Prepare agenda for 10 AM team meeting.",
                importance = Importance.HIGH,
                timestamp = Instant.parse("2024-11-10T09:00:00Z").toEpochMilli()
            ),
            Note(
                id = 5,
                title = "Dentist Appointment",
                content = "Dentist appointment on November 15th at 3 PM.",
                importance = Importance.HIGH,
                timestamp = Instant.parse("2024-11-10T12:45:00Z").toEpochMilli()
            ),
            Note(
                id = 6,
                title = "To-Do List",
                content = "Finish writing report, review email drafts, clean office.",
                importance = Importance.MEDIUM,
                timestamp = Instant.parse("2024-11-10T14:30:00Z").toEpochMilli()
            ),
            Note(
                id = 7,
                title = "Birthday Gift Idea",
                content = "Buy a gift for Sarah's birthday (something related to cooking).",
                importance = Importance.LOW,
                timestamp = Instant.parse("2024-11-10T16:00:00Z").toEpochMilli()
            ),
            Note(
                id = 8,
                title = "Car Maintenance",
                content = "Schedule oil change for the car this week.",
                importance = Importance.MEDIUM,
                timestamp = Instant.parse("2024-11-11T10:15:00Z").toEpochMilli()
            ),
            Note(
                id = 9,
                title = "Meeting with John",
                content = "Discuss project progress with John tomorrow at 2 PM.",
                importance = Importance.HIGH,
                timestamp = Instant.parse("2024-11-11T12:00:00Z").toEpochMilli()
            ),
            Note(
                id = 10,
                title = "Vacation Planning",
                content = "Research flight options for trip to Hawaii next summer.",
                importance = Importance.LOW,
                timestamp = Instant.parse("2024-11-11T13:30:00Z").toEpochMilli()
            ),

            Note(
                id = 9,
                title = "Work Anniversary",
                content = "Send congratulatory message to team for 5-year work anniversary.",
                importance = Importance.MEDIUM,
                timestamp = Instant.parse("2024-11-12T09:00:00Z").toEpochMilli()
            ) ,
            Note(
                id = 10,
                title = "Work Anniversary",
                content = "Send congratulatory message to team for 5-year work anniversary.",
                importance = Importance.MEDIUM,
                timestamp = Instant.parse("2024-11-12T09:00:00Z").toEpochMilli()
        )


        )
    }


    // Initialize notes with sample data on app start
    fun initializeNotes() {
        if (notes.isEmpty()) {

            notes.addAll(getSampleNotes())
        }
    }
}


