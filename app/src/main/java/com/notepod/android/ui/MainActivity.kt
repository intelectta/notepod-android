package com.notepod.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.notepod.android.R
import com.notepod.android.data.NoteDatabase
import com.notepod.android.data.NoteRepository
import com.notepod.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var adapter: NoteTreeAdapter
    private val vm: MainViewModel by viewModels {
        MainViewModelFactory(NoteRepository(NoteDatabase.getInstance(applicationContext).noteDao()))
    }
    private val editorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { vm.refreshTree() }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.title = "NotePod"

        adapter = NoteTreeAdapter(
            onClick      = { f -> openEditor(f.note.id, f.note.title, f.note.content) },
            onLongClick  = { f -> showDialog(f.note.id) },
            onExpand     = { f -> vm.toggleExpand(f.note) },
            onDelete     = { f ->
                AlertDialog.Builder(this)
                    .setTitle("Delete?")
                    .setMessage(if (f.childCount > 0) "Also deletes ${f.childCount} children." else "Cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> vm.deleteNote(f.note.id) }
                    .setNegativeButton("Cancel") { _, _ -> adapter.notifyDataSetChanged() }
                    .show()
            }
        )
        b.recyclerView.layoutManager = LinearLayoutManager(this)
        b.recyclerView.adapter = adapter
        adapter.attachSwipe(b.recyclerView)
        b.fab.setOnClickListener { showDialog(null) }

        vm.flatTree.observe(this) { nodes ->
            adapter.submitList(nodes)
            b.emptyState.visibility = if (nodes.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.loading.observe(this) { loading ->
            b.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun showDialog(parentId: String?) {
        val input = EditText(this).apply { hint = "Note title"; setPadding(48, 24, 48, 24) }
        AlertDialog.Builder(this)
            .setTitle(if (parentId == null) "New note" else "New child note")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) vm.addNote(parentId, t)
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun openEditor(id: String, title: String, content: String) {
        editorLauncher.launch(Intent(this, EditorActivity::class.java).apply {
            putExtra(EditorActivity.EXTRA_NOTE_ID, id)
            putExtra(EditorActivity.EXTRA_TITLE, title)
            putExtra(EditorActivity.EXTRA_CONTENT, content)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu); return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add_root) { showDialog(null); return true }
        return super.onOptionsItemSelected(item)
    }
}
