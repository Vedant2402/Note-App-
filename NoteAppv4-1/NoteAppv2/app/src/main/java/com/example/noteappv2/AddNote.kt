package com.example.noteappv2

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.noteappv2.model.Importance
import com.example.noteappv2.viewModel.NoteManager
import androidx.compose.material.icons.filled.ArrowBack
import com.example.noteappv2.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    onSave: (title: String, content: String, importance: Importance) -> Unit,
    onCancel: () -> Unit,
    note: Note? = null // Note to edit, if provided
) {
    // State variables for the fields, with default values handled using Elvis operator (?:)
    var title by remember { mutableStateOf(note?.title ?: "Untitled Note") } // Default to "Untitled Note" if note is null or title is missing
    var content by remember { mutableStateOf(note?.content ?: "No content available") } // Default to "No content available" if content is missing
    var importance by remember { mutableStateOf(note?.importance ?: Importance.MEDIUM) } // Default to Importance.MEDIUM if importance is missing
    Log.d("AddNoteScreen", "Note: $note")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "Add Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { onCancel() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onSave(title, content, importance) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Text("Importance:")
            Importance.values().forEach { level ->
                Row {
                    RadioButton(
                        selected = (importance == level),
                        onClick = { importance = level }
                    )
                    Text(level.name)
                }
            }
        }
    }
}
