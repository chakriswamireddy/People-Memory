package com.peoplememory.app.model

data class Contact(
    var lookupKey: String = "",
    var displayName: String = "",
    var photoUri: String? = null,
    var memoryCount: Int = 0,
    var lastMemoryAt: Long = 0L
)
