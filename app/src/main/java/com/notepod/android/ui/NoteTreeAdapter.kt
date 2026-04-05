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

    inner class ViewHolder(private val b: ItemNoteTreeBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(flat: FlatNode) {
            // Indentation: 20dp per depth level
            val indentPx = (flat.depth * 20 *
                b.root.context.resources.displayMetrics.density + 0.5f).toInt()
            val lp = b.indentSpacer.layoutParams
            lp.width = indentPx
            b.indentSpacer.layoutParams = lp

            b.noteTitle.text = flat.note.title

            val preview = flat.note.content.replace("\n", " ")
            b.notePreview.text = if (preview.length > 60)
                preview.take(60) + "…" else preview

            if (flat.childCount > 0) {
                b.expandBtn.text = if (flat.note.isExpanded) "▾" else "▸"
                b.expandBtn.visibility = View.VISIBLE
                b.expandBtn.setOnClickListener { onExpandToggle(flat) }
            } else {
                b.expandBtn.visibility = View.INVISIBLE
            }

            if (flat.childCount > 0) {
                b.childCountBadge.text = flat.childCount.toString()
                b.childCountBadge.visibility = View.VISIBLE
            } else {
                b.childCountBadge.visibility = View.GONE
            }

            b.root.setOnClickListener { onNodeClick(flat) }
            b.root.setOnLongClickListener { onNodeLongClick(flat); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemNoteTreeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun attachSwipe(rv: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) {
                    onDelete(getItem(pos))
                }
            }
        }).attachToRecyclerView(rv)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FlatNode>() {
            override fun areItemsTheSame(a: FlatNode, b: FlatNode) =
                a.note.id == b.note.id
            override fun areContentsTheSame(a: FlatNode, b: FlatNode) = a == b
        }
    }
}
