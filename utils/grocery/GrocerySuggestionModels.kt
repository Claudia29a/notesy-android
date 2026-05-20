package com.example.notesy.utils.grocery

data class SuggestedItem(
    val storeName: String,
    val itemName: String,
    val timesSeen: Int = 1
) {
    val key: String
        get() = "$storeName|$itemName"
}

data class GrocerySuggestionDialogState(
    val visible: Boolean = false,
    val suggestions: List<SuggestedItem> = emptyList(),
    val selectedKeys: Set<String> = emptySet()
)