package com.notepod.android.data

import java.util.UUID

class NoteRepository(private val dao: NoteDao) {

    fun getRootNotes() = dao.getRootNotes()
    fun getChildren(parentId: String) = dao.getChildren(parentId)

    suspend fun getFlatTree(): List<FlatNode> {
        val result = mutableListOf<FlatNode>()
        walkTree(null, 0, result)
        return result
    }

    // Private recursive helper — avoids local-suspend-fun edge cases
    private suspend fun walkTree(
        parentId: String?,
        depth: Int,
        result: MutableList<FlatNode>
    ) {
        val notes = if (parentId == null) dao.getRootNotesSync()
                    else dao.getChildrenSync(parentId)
        for (note in notes) {
            val childCount = dao.childCount(note.id)
            result.add(FlatNode(note, depth, childCount))
            if (note.isExpanded && childCount > 0) {
                walkTree(note.id, depth + 1, result)
            }
        }
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

data class FlatNode(
    val note: Note,
    val depth: Int,
    val childCount: Int
)
