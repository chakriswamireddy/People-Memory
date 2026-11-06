package com.peoplememory.app.model

import java.util.UUID

data class CloudMemory(
    var memoryId: String = UUID.randomUUID().toString(),
    var contactLookupKey: String = "",
    var contactName: String = "",
    var title: String = "",
    var content: String = "",
    var category: String = MemoryCategory.PERSONAL.name,
    var sentiment: String? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var isPendingSync: Boolean = false
)
