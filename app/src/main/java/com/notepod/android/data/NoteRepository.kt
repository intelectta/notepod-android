package com.notepod.android.data

import java.util.UUID

/**
 * Single source of truth for note operations.
 * All coroutine-based – call from ViewModel scope.
 */
class NoteRepository(private val dao: NoteDao) {

    fun getRootNotes() = dao.getRootNotes()
    fun getChildren(parentId: String) = dao.getChildren(parentId)

    /** Flatten the entire tree depth-first for the RecyclerView adapter. */
    suspend fun getFlatTree(): List<FlatNode> {
        val result = mutableListOf<FlatNode>()
        suspend fun walk(parentId: String?, depth: Int) {
            val notes = if (parentId == null) dao.getRootNotesSync()
                        else dao.getChildrenSync(parentId)
            for (note in notes) {
                val childCount = dao.childCount(note.id)
                result.add(FlatNode(note, depth, childCount))
                if (note.isExpanded && childCount > 0) {
                    walk(note.id, depth + 1)
                }
            }
        }
        walk(null, 0)
        return result
    }

    suspend fun addNote(parentId: String?, title: String, content: String = ""): Note {
        val maxOrder = if (parentId == null) dao.maxRootOrder()
                       else dao.maxChildOrder(parentId)
        val note = Note(
            id = UUID.randomUUID().toString(),
            parentId = parentId,
            title = title,
            content = content,
            sortOrder = (maxOrder ?: -1) + 1
        )
        dao.insert(note)
        return note
    }

    suspend fun updateNote(note: Note) {
        dao.update(note.copy(updatedAt = System.currentTimeMillis()))
    }

    /** Recursively delete note and all descendants */
    suspend fun deleteSubtree(id: String) {
        val stack = ArrayDeque<String>()
        stack.add(id)
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            dao.getChildIds(current).forEach { stack.add(it) }
            dao.deleteById(current)
        }
    }

    suspend fun toggleExpand(note: Note) {
        dao.update(note.copy(isExpanded = !note.isExpanded))
    }

    suspend fun getNoteById(id: String) = dao.getNoteById(id)
}

/** A note decorated with tree metadata for the flat list adapter */
data class FlatNode(
    val note: Note,
    val depth: Int,
    val childCount: Int
)
