package com.momt.emojipanel.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.momt.emojipanel.R
import com.momt.emojipanel.emoji.EmojiData
import com.momt.emojipanel.emoji.EmojiUtils
import com.momt.emojipanel.utils.EventHandler
import com.momt.emojipanel.widgets.EmojiImageView

internal class EmojiListAdapter(
    val context: Context,
    val items: MutableList<String>,
    val headersTitles: Array<String>,
    val defaultSkinColor: String,
    val skinColors: HashMap<String, String>,
    var hasRecent: Boolean
) :
    RecyclerView.Adapter<MyViewHolder<String>>() {
    companion object {
        const val HEADER_VIEW_TYPE = 0
        const val EMOJI_VIEW_TYPE = 1
    }

    val itemClicked = EventHandler<EmojiListAdapter, ItemClickEventArgs>()
    val itemLongClicked = EventHandler<EmojiListAdapter, ItemClickEventArgs>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder<String> {
        return when (viewType) {
            EMOJI_VIEW_TYPE -> {
                val img = EmojiImageView(parent.context)
                img.layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
                val vh = EmojiViewHolder(img)
                vh.clicked += { _, args -> itemClicked(this@EmojiListAdapter, args) }
                vh.longClicked += { _, args -> itemLongClicked(this, args) }
                vh
            }
            HEADER_VIEW_TYPE -> {
                HeaderViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_emoji_header, parent, false) as TextView
                )
            }
            else -> throw IllegalStateException()
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].startsWith("H")) HEADER_VIEW_TYPE else EMOJI_VIEW_TYPE

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MyViewHolder<String>, position: Int) {
        holder.bind(items[position])
    }

    inner class HeaderViewHolder(val txt: TextView) : MyViewHolder<String>(txt) {
        override fun bind(item: String) {
            txt.text = headersTitles[item.substring(1).toInt()]
        }
    }

    inner class EmojiViewHolder(val img: EmojiImageView) : MyViewHolder<String>(img) {
        val clicked = EventHandler<EmojiViewHolder, ItemClickEventArgs>()
        val longClicked = EventHandler<EmojiViewHolder, ItemClickEventArgs>()

        init {
            img.setOnClickListener { clicked(this, ItemClickEventArgs(this)) }

            img.setOnLongClickListener {
                longClicked(this, ItemClickEventArgs(this))
                true
            }
        }

        override fun bind(item: String) {
            val colorable = EmojiData.emojiColoredMap.contains(item)
            if (colorable)
                img.emojiCode = EmojiUtils.setColorToCode(item, skinColors[item] ?: defaultSkinColor)
            else
                img.emojiCode = item
            img.isLongClickable = colorable
            img.showSkinColor = colorable
        }

    }
}