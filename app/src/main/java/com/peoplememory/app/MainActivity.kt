package com.peoplememory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peoplememory.app.data.auth.AuthRepository
import com.peoplememory.app.data.repository.FirestoreMemoryRepository
import com.peoplememory.app.model.CloudMemory
import com.peoplememory.app.ui.screens.AddEditMemoryBottomSheet
import com.peoplememory.app.ui.screens.ContactDetailScreen
import com.peoplememory.app.ui.screens.HomeScreen
import com.peoplememory.app.ui.viewmodel.ContactDetailViewModel
import com.peoplememory.app.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    PeopleMemoryApp()
                }
            }
        }
    }
}

private class HomeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(AuthRepository(), FirestoreMemoryRepository()) as T
    }
}

private class ContactDetailViewModelFactory(
    private val contactLookupKey: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactDetailViewModel(
            contactLookupKey,
            AuthRepository(),
            FirestoreMemoryRepository()
        ) as T
    }
}

@Composable
private fun PeopleMemoryApp() {
    var selectedContactKey by remember { mutableStateOf<String?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<CloudMemory?>(null) }

    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory())
    val homeState by homeViewModel.uiState.collectAsState()

    when (val contactKey = selectedContactKey) {
        null -> HomeScreen(
            viewModel = homeViewModel,
            onContactClick = { key -> selectedContactKey = key },
            onNavigateToBackup = { },
            onAddMemoryClick = { showAddSheet = true },
            onEditMemoryClick = { memory ->
                editingMemory = memory
                showAddSheet = true
            },
            onDeleteMemoryClick = { }
        )

        else -> ContactDetailScreen(
            viewModel = viewModel(
                key = contactKey,
                factory = ContactDetailViewModelFactory(contactKey)
            ),
            onBackClick = { selectedContactKey = null }
        )
    }

    if (showAddSheet) {
        AddEditMemoryBottomSheet(
            editingMemory = editingMemory,
            contacts = homeState.contacts,
            onDismiss = {
                showAddSheet = false
                editingMemory = null
            },
            onSave = { contactLookupKey, title, description, category, sentiment ->
                homeViewModel.saveMemory(contactLookupKey, title, description, category, sentiment)
                showAddSheet = false
                editingMemory = null
            }
        )
    }
}
