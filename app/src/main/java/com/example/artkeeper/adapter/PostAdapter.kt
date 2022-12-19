package com.example.artkeeper.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artkeeper.R
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.databinding.PostItemBinding
import java.text.DateFormat
import java.util.*

class PostAdapter(private var optionsMenuClickListener: OptionsMenuClickListener) :
    ListAdapter<Post, PostAdapter.PostAdatperViewHolder>(DiffCallback) {

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(post: Post, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdatperViewHolder {
        val viewHolder = PostAdatperViewHolder(
            PostItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostAdatperViewHolder, position: Int) {
        holder.bind(getItem(position), optionsMenuClickListener)
    }

    class PostAdatperViewHolder(private var binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, optionsMenuClickListener: OptionsMenuClickListener) {
            val date = Date(post.postTimestamp)
            // modificato da bitmap a URI
            Glide.with(binding.photoItem.context)
                .load(
                    post.imagePath.path
                )
                .error(R.drawable.ic_baseline_settings_24)
                .into(binding.photoItem)
            //binding.photoItem.setImageURI()
            binding.nickNameItem.text = post.nickName
            binding.likeItem.text = post.nLike.toString()
            binding.childNameItem.apply {
                if (post.sketchedBy == null)
                    visibility = View.GONE
                else
                    text = resources.getString(R.string.sketched_by, post.sketchedBy)
            }
            binding.descriptionItem.apply {
                if (post.description == null)
                    visibility = View.GONE
                else
                    text = post.description
            }
            binding.timestampPostItem.text =
                DateFormat.getDateInstance(DateFormat.SHORT).format(date)

            binding.textViewOptions.setOnClickListener {
                optionsMenuClickListener.onOptionsMenuClicked(post, adapterPosition)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }
}