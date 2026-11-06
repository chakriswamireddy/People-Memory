package com.peoplememory.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peoplememory.app.data.auth.AuthRepository
import com.peoplememory.app.data.repository.FirestoreMemoryRepository
import com.peoplememory.app.model.CloudMemory
import com.peoplememory.app.model.Contact
import com.peoplememory.app.model.MemoryCategory
import com.peoplememory.app.model.MemorySentiment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

data class HomeUiState(
    val isLoading: Boolean = true,
    val isSignedIn: Boolean = false,
    val memories: List<CloudMemory> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val totalMemoriesCount: Int = 0,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val memoryRepository: FirestoreMemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState()
                .flatMapLatest { user ->
                    if (user == null) {
                        flowOf(HomeUiState(isLoading = false, isSignedIn = false))
                    } else {
                        memoryRepository.observeMemories(user.uid).map { memories ->
                            buildState(memories)
                        }
                    }
                }
                .catch { e ->
                    emit(
                        HomeUiState(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load memories"
                        )
                    )
                }
                .collect { state -> _uiState.value = state }
        }
    }

    private fun buildState(memories: List<CloudMemory>): HomeUiState {
        val contacts = memories
            .groupBy { it.contactLookupKey }
            .map { (lookupKey, group) ->
                Contact(
                    lookupKey = lookupKey,
                    displayName = group.lastOrNull()?.contactName ?: "Unknown",
                    memoryCount = group.size,
                    lastMemoryAt = group.maxOf { it.createdAt }
                )
            }
            .sortedByDescending { it.lastMemoryAt }

        return HomeUiState(
            isLoading = false,
            isSignedIn = true,
            memories = memories,
            contacts = contacts,
            totalMemoriesCount = memories.size
        )
    }

    fun saveMemory(
        contactLookupKey: String,
        title: String,
        description: String,
        category: MemoryCategory,
        sentiment: MemorySentiment
    ) {
        val uid = authRepository.currentUser?.uid ?: return
        if (title.isBlank() && description.isBlank()) return

        val contactName = _uiState.value.contacts
            .find { it.lookupKey == contactLookupKey }
            ?.displayName ?: "Unknown"

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val memory = CloudMemory(
                memoryId = UUID.randomUUID().toString(),
                contactLookupKey = contactLookupKey,
                contactName = contactName,
                title = title.ifBlank { description.take(60) },
                content = description,
                category = category.name,
                sentiment = sentiment.name,
                createdAt = now,
                updatedAt = now
            )
            memoryRepository.saveMemory(uid, memory)
        }
    }
}
