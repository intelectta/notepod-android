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

    private lateinit var binding: ActivityEditorBinding
    private lateinit var noteId: String

    private val vm: MainViewModel by viewModels {
        MainViewModelFactory(NoteRepository(NoteDatabase.getInstance(applicationContext).noteDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: run { finish(); return }
        binding.editTitle.setText(intent.getStringExtra(EXTRA_TITLE) ?: "")
        binding.editContent.setText(intent.getStringExtra(EXTRA_CONTENT) ?: "")
    }

    private fun save() {
        val title = binding.editTitle.text.toString().trim().ifEmpty { "Untitled" }
        val content = binding.editContent.text.toString()
        vm.saveNote(noteId, title, content)
    }

    override fun onSupportNavigateUp(): Boolean {
        save(); finish(); return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        save()
        @Suppress("DEPRECATION")
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
