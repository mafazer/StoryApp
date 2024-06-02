package com.mafazer.storyappsubmission.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mafazer.storyappsubmission.data.local.DbListStoryItem
import com.mafazer.storyappsubmission.databinding.ItemStoryBinding

class StoryListAdapter(private val onItemClick: (String) -> Unit) :
    PagingDataAdapter<DbListStoryItem, StoryListAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DbListStoryItem>() {
            override fun areItemsTheSame(
                oldItem: DbListStoryItem,
                newItem: DbListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DbListStoryItem,
                newItem: DbListStoryItem
            ): Boolean {
                return oldItem == newItem
            }
        }

        fun getDiffCallback() = DIFF_CALLBACK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: DbListStoryItem) {
            binding.apply {
                tvItemName.text = story.name
                tvItemDescription.text = story.description
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(ivItemPhoto)
                itemView.setOnClickListener {
                    onItemClick(story.id)
                }
            }
        }
    }
}