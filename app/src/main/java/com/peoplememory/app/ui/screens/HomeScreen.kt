package com.peoplememory.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peoplememory.app.model.CloudMemory
import com.peoplememory.app.model.Contact
import com.peoplememory.app.model.MemorySentiment
import com.peoplememory.app.ui.viewmodel.HomeUiState
import com.peoplememory.app.ui.viewmodel.HomeViewModel

enum class HomeTab {
    MEMORIES,
    CONTACTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onContactClick: (String) -> Unit,
    onNavigateToBackup: () -> Unit,
    onAddMemoryClick: (String?) -> Unit = {},
    onEditMemoryClick: (CloudMemory) -> Unit = {},
    onDeleteMemoryClick: (CloudMemory) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(HomeTab.MEMORIES) }
    var memoriesSearchQuery by remember { mutableStateOf("") }
    var selectedLabelFilter by remember { mutableStateOf<MemorySentiment?>(null) } // null = All

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("People Memory", fontWeight = FontWeight.Bold)
                        Text(
                            "${uiState.totalMemoriesCount} memories • On-Device AI",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddMemoryClick(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Memory")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Top-Level Navigation Tabs: Memories (Default) vs Contacts
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == HomeTab.MEMORIES,
                    onClick = { selectedTab = HomeTab.MEMORIES },
                    text = { Text("Memories") }
                )
                Tab(
                    selected = selectedTab == HomeTab.CONTACTS,
                    onClick = { selectedTab = HomeTab.CONTACTS },
                    text = { Text("Contacts") }
                )
            }

            if (selectedTab == HomeTab.MEMORIES) {
                MemoriesTabContent(
                    memories = uiState.memories,
                    contacts = uiState.contacts,
                    searchQuery = memoriesSearchQuery,
                    onSearchQueryChange = { memoriesSearchQuery = it },
                    selectedLabelFilter = selectedLabelFilter,
                    onLabelFilterChange = { selectedLabelFilter = it },
                    onContactClick = onContactClick,
                    onEditMemoryClick = onEditMemoryClick,
                    onDeleteMemoryClick = onDeleteMemoryClick
                )
            } else {
                ContactsTabContent(uiState = uiState, onContactClick = onContactClick)
            }
        }
    }
}