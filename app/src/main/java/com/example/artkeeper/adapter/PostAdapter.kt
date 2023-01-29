package com.example.artkeeper.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.artkeeper.R
import com.example.artkeeper.data.model.Post
import com.example.artkeeper.databinding.PostItemBinding
import java.text.DateFormat
import java.util.*

class PostAdapter(
    private val clickListener: PostListener
) :
    ListAdapter<Post, PostAdapter.PostItemViewHolder>(PostRemoteDiffCallback()) {

    var nickName: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class PostItemViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            post: Post,
            nickName: String,
            clickListener: PostListener
        ) {
            binding.apply {
                nickNameItem.text = nickName
                /*
                childNameItem.apply {
                    if (post.sketchedBy != null)
                        text = post.sketchedBy
                    else
                        visibility = View.GONE
                }*/
                childNameItem.apply {
                    if (post.sketchedBy.isNullOrEmpty() || post.sketchedBy.equals("null"))
                        visibility = View.GONE
                    else
                        text = resources.getString(R.string.sketched_by, post.sketchedBy)
                }
                descriptionItem.apply {
                    if (post.description.isNullOrEmpty())
                        visibility = View.GONE
                    else
                        text = post.description
                }
                timestampPostItem.text =
                    DateFormat.getDateInstance(DateFormat.SHORT)
                        .format(Date(post.timestamp.toLong()))

                Glide.with(photoItem.context)
                    .load(post.imagePath.toUri())
                    .format(DecodeFormat.PREFER_RGB_565)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.drawable.ic_baseline_settings_24)
                    .into(photoItem)

                textViewOptions.setOnClickListener {
                    clickListener.clickListener(post, adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostItemViewHolder {
        return PostItemViewHolder(
            PostItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PostItemViewHolder, position: Int) {

        holder.bind(getItem(position), nickName, clickListener)

    }

    class PostListener(val clickListener: (post: Post, position: Int) -> Unit)

    class PostRemoteDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem == newItem
        }

    }
}