package com.example.noteappv2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.noteappv2.model.Importance
import com.example.noteappv2.model.Note
import com.example.noteappv2.ui.theme.NoteAppv2Theme
import com.example.noteappv2.viewModel.NoteManager
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this);
        setContent {
            NoteAppv2Theme {
                NoteApp()
            }
        }
    }
}


@Composable
fun NoteApp() {
    var showAddNoteScreen by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(emptyList<Note>()) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }

    // Real-time fetching of notes
    LaunchedEffect(Unit) {
        NoteManager.fetchNotesRealTime(
            onUpdate = { updatedNotes ->
                notes = updatedNotes
            },
            onFailure = { exception ->
                Log.e("NoteApp", "Failed to fetch notes: ${exception.message}")
            }
        )
    }

    if (showAddNoteScreen) {
        AddNoteScreen(
            note = noteToEdit, // Pass noteToEdit if editing, otherwise null
            onSave = { title, content, importance ->
                if (noteToEdit != null) {
                    // Update note
                    NoteManager.updateNote(
                        noteId = noteToEdit!!.id,
                        title = title,
                        content = content,
                        importance = importance,
                        onSuccess = {
                            noteToEdit = null
                            showAddNoteScreen = false
                        },
                        onFailure = { exception ->
                            Log.e("NoteApp", "Failed to update note: ${exception.message}")
                        }
                    )
                } else {
                    // Add new note
                    NoteManager.addNote(
                        title = title,
                        content = content,
                        importance = importance
                    )
                    showAddNoteScreen = false
                }
            },
            onCancel = {
                noteToEdit = null
                showAddNoteScreen = false
            }
        )
    } else {
        MainNoteScreen(
            notes = notes,
            onAddNoteClicked = {
                noteToEdit = null // Clear noteToEdit for a new note
                showAddNoteScreen = true
            },
            onNoteClicked = { note ->
                noteToEdit = note // Set noteToEdit to the clicked note
                showAddNoteScreen = true
            }
        )
    }
}






@Composable
fun NoteList(notes: List<Note>, onNoteClicked: (Note) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(notes) { note ->
            NoteCard(
                note = note,
                modifier = Modifier.padding(3.dp),
                onClick = onNoteClicked // Handle note click
            )
        }
    }
}



@Composable
fun NoteCard(note: Note, modifier: Modifier = Modifier, onClick: (Note) -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(note.timestamp))

    val cardColor = when (note.importance) {
        Importance.URGENT -> Color(0xFFFF8A80)
        Importance.HIGH -> Color(0xFFFF9E80)
        Importance.MEDIUM -> Color(0xFFFFE57F)
        Importance.LOW -> Color(0xFFB9F6CA)
    }

    val importanceSymbol = when (note.importance) {
        Importance.URGENT -> "⚠️"
        Importance.HIGH -> "🔥"
        Importance.MEDIUM -> "📌"
        Importance.LOW -> "✅"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(note) }, // Pass the note to the onClick callback
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = modifier.padding(10.dp)) {
            Text(
                text = "$importanceSymbol  ${note.title}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF231F20),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Last updated: $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray
            )
            Text(
                text = "Delete",
                color = Color.Red,
                modifier = Modifier.clickable {
                    Log.d("NoteCard", "Delete clicked for note ID: ${note.id}")

                    // Call deleteNote from NoteManager
                    NoteManager.deleteNote(
                        noteId = note.id,
                        onSuccess = {
                            Log.d("NoteCard", "Note with ID ${note.id} successfully deleted")
                        },
                        onFailure = { exception ->
                            Log.e("NoteCard", "Failed to delete note: ${exception.message}")
                        }
                    )
                }
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNoteScreen(notes: List<Note>, onAddNoteClicked: () -> Unit, onNoteClicked: (Note) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(text = "Note App") }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { onAddNoteClicked() },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add note")
                    }
                }
            )
        }
    ) { innerPadding ->
        NoteList(
            notes = notes,
            onNoteClicked = onNoteClicked,
            modifier = Modifier.padding(innerPadding)
        )
    }
}


@Preview
@Composable
private fun NoteAppPreview() {
    NoteApp()
}
