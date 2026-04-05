package com.notepod.android.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE parentId IS NULL ORDER BY sortOrder ASC")
    fun getRootNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE parentId IS NULL ORDER BY sortOrder ASC")
    suspend fun getRootNotesSync(): List<Note>

    @Query("SELECT * FROM notes WHERE parentId = :parentId ORDER BY sortOrder ASC")
    suspend fun getChildrenSync(parentId: String): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT COUNT(*) FROM notes WHERE parentId = :parentId")
    suspend fun childCount(parentId: String): Int

    @Query("SELECT MAX(sortOrder) FROM notes WHERE parentId IS NULL")
    suspend fun maxRootOrder(): Int?

    @Query("SELECT MAX(sortOrder) FROM notes WHERE parentId = :parentId")
    suspend fun maxChildOrder(parentId: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT id FROM notes WHERE parentId = :parentId")
    suspend fun getChildIds(parentId: String): List<String>
}
