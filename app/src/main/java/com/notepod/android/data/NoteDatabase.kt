package com.notepod.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, NoteDatabase::class.java, "notepod.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed a welcome document on first run
                        CoroutineScope(Dispatchers.IO).launch {
                            seedWelcomeData(getInstance(context).noteDao())
                        }
                    }
                })
                .build()

        private suspend fun seedWelcomeData(dao: NoteDao) {
            val rootId = UUID.randomUUID().toString()
            dao.insert(Note(id = rootId, parentId = null,
                title = "Welcome to NotePod", sortOrder = 0,
                content = "This is your first document.\n\nTap a node to edit it.\nLong-press a node to add a child."))

            val child1 = UUID.randomUUID().toString()
            dao.insert(Note(id = child1, parentId = rootId,
                title = "Getting Started", sortOrder = 0,
                content = "• Tap ✏️ to edit this note\n• Use the + FAB to add siblings\n• Long-press to add a child node\n• Swipe left to delete"))

            val child2 = UUID.randomUUID().toString()
            dao.insert(Note(id = child2, parentId = rootId,
                title = "Features (MVP)", sortOrder = 1,
                content = "✅ Hierarchical tree\n✅ Rich text editor\n✅ Local persistence\n✅ Expand / collapse\n✅ Add / delete nodes"))

            dao.insert(Note(id = UUID.randomUUID().toString(), parentId = child2,
                title = "Coming next", sortOrder = 0,
                content = "See the upgrade roadmap in the README."))
        }
    }
}
