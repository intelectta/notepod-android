package com.notepod.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notepod.android.data.FlatNode
import com.notepod.android.databinding.ItemNoteTreeBinding

class NoteTreeAdapter(
    private val onNodeClick: (FlatNode) -> Unit,
    private val onNodeLongClick: (FlatNode) -> Unit,
    private val onExpandToggle: (FlatNode) -> Unit,
    private val onDelete: (FlatNode) -> Unit
) : ListAdapter<FlatNode, NoteTreeAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemNoteTreeBinding)
        : RecyclerView.ViewHolder(b.root) {

        fun bind(flat: FlatNode) {
            val density = b.root.context.resources.displayMetrics.density
            val indentPx = (flat.depth * 20 * density + 0.5f).toInt()
            val lp = b.indentSpacer.layoutParams
            lp.width = indentPx
            b.indentSpacer.layoutParams = lp

            b.noteTitle.text = flat.note.title

            val raw = flat.note.content.replace("\n", " ")
            b.notePreview.text = if (raw.length > 60) raw.take(60) + "\u2026" else raw

            if (flat.childCount > 0) {
                b.expandBtn.visibility = View.VISIBLE
                b.expandBtn.text = if (flat.note.isExpanded) "\u25be" else "\u25b8"
                b.expandBtn.setOnClickListener { onExpandToggle(flat) }
                b.childCountBadge.visibility = View.VISIBLE
                b.childCountBadge.text = flat.childCount.toString()
            } else {
                b.expandBtn.visibility = View.INVISIBLE
                b.childCountBadge.visibility = View.GONE
            }

            b.root.setOnClickListener { onNodeClick(flat) }
            b.root.setOnLongClickListener { onNodeLongClick(flat); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoteTreeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun attachSwipe(rv: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos >= 0) {
                    onDelete(getItem(pos))
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(rv)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FlatNode>() {
            override fun areItemsTheSame(oldItem: FlatNode, newItem: FlatNode): Boolean =
                oldItem.note.id == newItem.note.id
            override fun areContentsTheSame(oldItem: FlatNode, newItem: FlatNode): Boolean =
                oldItem == newItem
        }
    }
}
