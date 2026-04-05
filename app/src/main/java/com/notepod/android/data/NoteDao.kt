package com.notepod.android.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    // ── Queries ──────────────────────────────────────────────────────────────

    /** All root-level notes ordered by sortOrder */
    @Query("SELECT * FROM notes WHERE parentId IS NULL ORDER BY sortOrder ASC")
    fun getRootNotes(): LiveData<List<Note>>

    /** All children of a specific parent */
    @Query("SELECT * FROM notes WHERE parentId = :parentId ORDER BY sortOrder ASC")
    fun getChildren(parentId: String): LiveData<List<Note>>

    /** Suspend version used by tree flattening algorithm */
    @Query("SELECT * FROM notes WHERE parentId = :parentId ORDER BY sortOrder ASC")
    suspend fun getChildrenSync(parentId: String): List<Note>

    @Query("SELECT * FROM notes WHERE parentId IS NULL ORDER BY sortOrder ASC")
    suspend fun getRootNotesSync(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT COUNT(*) FROM notes WHERE parentId = :parentId")
    suspend fun childCount(parentId: String): Int

    @Query("SELECT MAX(sortOrder) FROM notes WHERE parentId IS NULL")
    suspend fun maxRootOrder(): Int?

    @Query("SELECT MAX(sortOrder) FROM notes WHERE parentId = :parentId")
    suspend fun maxChildOrder(parentId: String): Int?

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Recursively delete a subtree – called in a loop by the repository */
    @Query("SELECT id FROM notes WHERE parentId = :parentId")
    suspend fun getChildIds(parentId: String): List<String>
}
