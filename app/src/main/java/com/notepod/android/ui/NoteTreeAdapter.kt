package com.notepod.android.ui

import android.view.LayoutInflater
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
    private val onDelete: (FlatNode) -> Unit,
) : ListAdapter<FlatNode, NoteTreeAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val b: ItemNoteTreeBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(flat: FlatNode) {
            // Indentation: 20dp per depth level
            val indent = flat.depth * 20
            b.indentSpacer.layoutParams =
                b.indentSpacer.layoutParams.also { it.width = indent.dpToPx(b.root) }

            b.noteTitle.text = flat.note.title
            b.notePreview.text = flat.note.content
                .replace("\n", " ").take(60)
                .let { if (flat.note.content.length > 60) "$it…" else it }

            // Expand/collapse chevron
            if (flat.childCount > 0) {
                b.expandBtn.text = if (flat.note.isExpanded) "▾" else "▸"
                b.expandBtn.visibility = android.view.View.VISIBLE
                b.expandBtn.setOnClickListener { onExpandToggle(flat) }
            } else {
                b.expandBtn.visibility = android.view.View.INVISIBLE
            }

            b.childCountBadge.text = if (flat.childCount > 0) flat.childCount.toString() else ""
            b.childCountBadge.visibility =
                if (flat.childCount > 0) android.view.View.VISIBLE else android.view.View.GONE

            b.root.setOnClickListener { onNodeClick(flat) }
            b.root.setOnLongClickListener { onNodeLongClick(flat); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemNoteTreeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /** Attach swipe-to-delete to the RecyclerView */
    fun attachSwipe(rv: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                onDelete(getItem(vh.adapterPosition))
            }
        }).attachToRecyclerView(rv)
    }

    private fun Int.dpToPx(view: android.view.View): Int =
        (this * view.context.resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FlatNode>() {
            override fun areItemsTheSame(a: FlatNode, b: FlatNode) = a.note.id == b.note.id
            override fun areContentsTheSame(a: FlatNode, b: FlatNode) = a == b
        }
    }
}
