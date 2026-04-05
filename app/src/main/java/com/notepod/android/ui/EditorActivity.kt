package com.notepod.android.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.notepod.android.R
import com.notepod.android.data.NoteDatabase
import com.notepod.android.data.NoteRepository
import com.notepod.android.databinding.ActivityEditorBinding

/**
 * Full-screen editor for a single note.
 * Saves on back-press / up-press automatically.
 */
class EditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_TITLE   = "title"
        const val EXTRA_CONTENT = "content"
    }

    private lateinit var b: ActivityEditorBinding
    private lateinit var noteId: String

    private val vm: MainViewModel by viewModels {
        val repo = NoteRepository(NoteDatabase.getInstance(applicationContext).noteDao())
        MainViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: run { finish(); return }
        b.editTitle.setText(intent.getStringExtra(EXTRA_TITLE) ?: "")
        b.editContent.setText(intent.getStringExtra(EXTRA_CONTENT) ?: "")

        supportActionBar?.title = ""  // title lives in the EditText
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private fun save() {
        val title = b.editTitle.text.toString().trim().ifEmpty { "Untitled" }
        val content = b.editContent.text.toString()
        vm.saveNote(noteId, title, content)
    }

    // ── Navigation / menu ─────────────────────────────────────────────────────

    override fun onSupportNavigateUp(): Boolean {
        save()
        finish()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        save()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> { save(); finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
