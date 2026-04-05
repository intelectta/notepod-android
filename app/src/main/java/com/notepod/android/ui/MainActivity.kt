package com.notepod.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
    private val vm: MainViewModel by viewModels {
        val repo = NoteRepository(NoteDatabase.getInstance(applicationContext).noteDao())
        MainViewModelFactory(repo)
    }

    private lateinit var adapter: NoteTreeAdapter

    // Re-use the same launcher for both new-note and edit flows
    private val editorLauncher = registerForActivityResult(StartActivityForResult()) {
        vm.refreshTree()   // refresh regardless of result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.title = "NotePod"

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = NoteTreeAdapter(
            onNodeClick      = { flat -> openEditor(flat.note.id, flat.note.title, flat.note.content) },
            onNodeLongClick  = { flat -> showAddChildDialog(flat.note.id) },
            onExpandToggle   = { flat -> vm.toggleExpand(flat.note) },
            onDelete         = { flat ->
                AlertDialog.Builder(this)
                    .setTitle("Delete "${flat.note.title}"?")
                    .setMessage(
                        if (flat.childCount > 0)
                            "This will also delete ${flat.childCount} child node(s)."
                        else
                            "This action cannot be undone."
                    )
                    .setPositiveButton("Delete") { _, _ -> vm.deleteNote(flat.note.id) }
                    .setNegativeButton("Cancel") { _, _ -> adapter.notifyDataSetChanged() }
                    .show()
            }
        )
        b.recyclerView.layoutManager = LinearLayoutManager(this)
        b.recyclerView.adapter = adapter
        adapter.attachSwipe(b.recyclerView)
    }

    // ── FAB ──────────────────────────────────────────────────────────────────

    private fun setupFab() {
        b.fab.setOnClickListener {
            showAddNoteDialog(parentId = null, hint = "New root note")
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private fun showAddNoteDialog(parentId: String?, hint: String) {
        val input = EditText(this).apply {
            setHint(hint)
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle(if (parentId == null) "New note" else "New child note")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) vm.addNote(parentId, title)
            }
            .setNegativeButton("Cancel", null)
            .show()
        // Open keyboard
        input.requestFocus()
    }

    private fun showAddChildDialog(parentId: String) {
        showAddNoteDialog(parentId, "Child note title")
    }

    // ── Editor ────────────────────────────────────────────────────────────────

    private fun openEditor(id: String, title: String, content: String) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra(EditorActivity.EXTRA_NOTE_ID, id)
            putExtra(EditorActivity.EXTRA_TITLE, title)
            putExtra(EditorActivity.EXTRA_CONTENT, content)
        }
        editorLauncher.launch(intent)
    }

    // ── ViewModel ─────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        vm.flatTree.observe(this) { nodes ->
            adapter.submitList(nodes)
            b.emptyState.visibility =
                if (nodes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        vm.loading.observe(this) { loading ->
            b.progressBar.visibility =
                if (loading) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    // ── Options menu ──────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_root -> { showAddNoteDialog(null, "New root note"); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
