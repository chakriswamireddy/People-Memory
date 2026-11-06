package com.peoplememory.app.model

import kotlinx.serialization.Serializable

@Serializable
data class ContactSummary(
    val contactLookupKey: String,
    val contactName: String,
    val summaryText: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val memoryCountAtGeneration: Int = 0,
    val isOutdated: Boolean = false
)