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

class EditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_TITLE   = "title"
        const val EXTRA_CONTENT = "content"
    }

    private lateinit var b: ActivityEditorBinding
    private lateinit var noteId: String
    private val vm: MainViewModel by viewModels {
        MainViewModelFactory(NoteRepository(NoteDatabase.getInstance(applicationContext).noteDao()))
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: run { finish(); return }
        b.editTitle.setText(intent.getStringExtra(EXTRA_TITLE) ?: "")
        b.editContent.setText(intent.getStringExtra(EXTRA_CONTENT) ?: "")
    }

    private fun save() {
        vm.saveNote(noteId,
            b.editTitle.text.toString().trim().ifEmpty { "Untitled" },
            b.editContent.text.toString())
    }

    override fun onSupportNavigateUp(): Boolean { save(); finish(); return true }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { save(); @Suppress("DEPRECATION") super.onBackPressed() }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editor, menu); return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) { save(); finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
