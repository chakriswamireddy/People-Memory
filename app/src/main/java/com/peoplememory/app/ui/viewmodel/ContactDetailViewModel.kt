package com.peoplememory.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peoplememory.app.data.auth.AuthRepository
import com.peoplememory.app.data.repository.FirestoreMemoryRepository
import com.peoplememory.app.model.CloudMemory
import com.peoplememory.app.model.Contact
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ContactDetailUiState(
    val isLoading: Boolean = true,
    val contact: Contact? = null,
    val memories: List<CloudMemory> = emptyList(),
    val aiSummary: String? = null,
    val isGeneratingSummary: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ContactDetailViewModel(
    private val contactLookupKey: String,
    private val authRepository: AuthRepository,
    private val memoryRepository: FirestoreMemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthState()
                .flatMapLatest { user ->
                    if (user == null) {
                        flowOf(ContactDetailUiState(isLoading = false))
                    } else {
                        memoryRepository.observeMemories(user.uid).map { all ->
                            val memories = all.filter { it.contactLookupKey == contactLookupKey }
                            ContactDetailUiState(
                                isLoading = false,
                                contact = memories.firstOrNull()?.let { first ->
                                    Contact(
                                        lookupKey = contactLookupKey,
                                        displayName = first.contactName,
                                        memoryCount = memories.size,
                                        lastMemoryAt = memories.maxOf { it.createdAt }
                                    )
                                } ?: Contact(
                                    lookupKey = contactLookupKey,
                                    displayName = "Unknown",
                                    memoryCount = memories.size
                                ),
                                memories = memories
                            )
                        }
                    }
                }
                .catch { e ->
                    emit(
                        ContactDetailUiState(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    )
                }
                .collect { state -> _uiState.value = state }
        }
    }
}
