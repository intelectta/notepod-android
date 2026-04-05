package com.notepod.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core data model – mirrors the Windows app's node structure:
 *   { id, title, content, children[] }
 *
 * Android adaptation: children relationship expressed via parentId (adjacency list).
 * sortOrder keeps siblings ordered as the user arranges them.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,           // UUID, matches .iectta format
    val parentId: String?,                // null = root-level node
    val title: String,
    val content: String = "",
    val sortOrder: Int = 0,               // position among siblings
    val isExpanded: Boolean = true,       // UI expansion state
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
