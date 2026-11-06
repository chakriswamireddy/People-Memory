package com.peoplememory.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peoplememory.app.ai.LocalAIService
import com.peoplememory.app.model.CloudMemory
import com.peoplememory.app.model.Contact
import com.peoplememory.app.model.MemoryCategory
import com.peoplememory.app.model.MemorySentiment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemoryBottomSheet(
    editingMemory: CloudMemory?,
    contacts: List<Contact> = emptyList(),
    defaultContactLookupKey: String? = null,
    localAIService: LocalAIService? = null,
    onDismiss: () -> Unit,
    onSave: (contactLookupKey: String, title: String, description: String, category: MemoryCategory, sentiment: MemorySentiment) -> Unit
) {
    var selectedContactLookup by remember {
        mutableStateOf(editingMemory?.contactLookupKey ?: defaultContactLookupKey ?: contacts.firstOrNull()?.lookupKey ?: "")
    }
    var title by remember { mutableStateOf(editingMemory?.title ?: "") }
    var description by remember { mutableStateOf(editingMemory?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(MemoryCategory.PERSONAL) }
    var selectedSentiment by remember {
        mutableStateOf(MemorySentiment.fromString(editingMemory?.sentiment ?: MemorySentiment.CARED_ABOUT_YOU.name))
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Record Memory", style = MaterialTheme.typography.titleLarge)
            // Contact picker (hidden if created from contact profile), 4 emotional label chips, category, title, description & On-device AI rewrite
        }
    }
}