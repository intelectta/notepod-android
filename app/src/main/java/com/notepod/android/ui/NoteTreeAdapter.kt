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
    private val onClick: (FlatNode) -> Unit,
    private val onLongClick: (FlatNode) -> Unit,
    private val onExpand: (FlatNode) -> Unit,
    private val onDelete: (FlatNode) -> Unit
) : ListAdapter<FlatNode, NoteTreeAdapter.VH>(DIFF) {

    inner class VH(val b: ItemNoteTreeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(f: FlatNode) {
            val px = (f.depth * 20 * b.root.context.resources.displayMetrics.density + 0.5f).toInt()
            b.indentSpacer.layoutParams = b.indentSpacer.layoutParams.also { it.width = px }
            b.noteTitle.text = f.note.title
            val prev = f.note.content.replace("\n", " ")
            b.notePreview.text = if (prev.length > 60) prev.take(60) + "..." else prev
            if (f.childCount > 0) {
                b.expandBtn.visibility = View.VISIBLE
                b.expandBtn.text = if (f.note.isExpanded) "v" else ">"
                b.expandBtn.setOnClickListener { onExpand(f) }
                b.childCountBadge.visibility = View.VISIBLE
                b.childCountBadge.text = f.childCount.toString()
            } else {
                b.expandBtn.visibility = View.INVISIBLE
                b.childCountBadge.visibility = View.GONE
            }
            b.root.setOnClickListener { onClick(f) }
            b.root.setOnLongClickListener { onLongClick(f); true }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemNoteTreeBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    fun attachSwipe(rv: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder,
                                t: RecyclerView.ViewHolder) = false
            override fun onSwiped(v: RecyclerView.ViewHolder, d: Int) {
                val p = v.bindingAdapterPosition
                if (p >= 0) onDelete(getItem(p))
            }
        }).attachToRecyclerView(rv)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FlatNode>() {
            override fun areItemsTheSame(a: FlatNode, b: FlatNode) = a.note.id == b.note.id
            override fun areContentsTheSame(a: FlatNode, b: FlatNode) = a == b
        }
    }
}
