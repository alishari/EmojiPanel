package com.momt.emojipanel.adapters

import android.content.Context
import android.view.View

class ItemClickEventArgs(
    viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder
) {
    val position: Int = viewHolder.adapterPosition
    val view: View = viewHolder.itemView
}

abstract class MyViewHolder<T>(private val containerView: View) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(containerView) {
    val context: Context
        get() = containerView.context

    abstract fun bind(item: T)
}
