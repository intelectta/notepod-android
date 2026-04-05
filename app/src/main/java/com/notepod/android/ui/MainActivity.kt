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

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteTreeAdapter

    private val vm: MainViewModel by viewModels {
        MainViewModelFactory(NoteRepository(NoteDatabase.getInstance(applicationContext).noteDao()))
    }

    private val editorLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { vm.refreshTree() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "NotePod"
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = NoteTreeAdapter(
            onNodeClick     = { flat -> openEditor(flat.note.id, flat.note.title, flat.note.content) },
            onNodeLongClick = { flat -> showAddDialog(flat.note.id) },
            onExpandToggle  = { flat -> vm.toggleExpand(flat.note) },
            onDelete        = { flat ->
                AlertDialog.Builder(this)
                    .setTitle("Delete \"${flat.note.title}\"?")
                    .setMessage(if (flat.childCount > 0)
                        "This also deletes ${flat.childCount} child node(s)."
                        else "This cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> vm.deleteNote(flat.note.id) }
                    .setNegativeButton("Cancel") { _, _ -> adapter.notifyDataSetChanged() }
                    .show()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        adapter.attachSwipe(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener { showAddDialog(null) }
    }

    private fun showAddDialog(parentId: String?) {
        val input = EditText(this).apply {
            hint = if (parentId == null) "Root note title" else "Child note title"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(this)
            .setTitle(if (parentId == null) "New note" else "New child note")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) vm.addNote(parentId, t)
            }
            .setNegativeButton("Cancel", null)
            .show()
        input.requestFocus()
    }

    private fun openEditor(id: String, title: String, content: String) {
        editorLauncher.launch(
            Intent(this, EditorActivity::class.java).apply {
                putExtra(EditorActivity.EXTRA_NOTE_ID, id)
                putExtra(EditorActivity.EXTRA_TITLE, title)
                putExtra(EditorActivity.EXTRA_CONTENT, content)
            }
        )
    }

    private fun observeViewModel() {
        vm.flatTree.observe(this) { nodes ->
            adapter.submitList(nodes)
            binding.emptyState.visibility = if (nodes.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_root -> { showAddDialog(null); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
