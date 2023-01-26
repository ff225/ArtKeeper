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
import com.example.artkeeper.data.model.PostRemote
import com.example.artkeeper.databinding.PostItemBinding
import java.text.DateFormat
import java.util.*

class PostAdapter :
    ListAdapter<PostRemote, PostAdapter.PostItemViewHolder>(PostRemoteDiffCallback()) {

    var nickName: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class PostItemViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostRemote, nickName: String) {

            binding.nickNameItem.text = nickName
            binding.childNameItem.apply {
                if (post.sketchedBy != null)
                    text = post.sketchedBy
                else
                    visibility = View.GONE
            }
            binding.childNameItem.apply {
                if (post.sketchedBy.isNullOrEmpty() || post.sketchedBy.equals("null"))
                    visibility = View.GONE
                else
                    text = resources.getString(R.string.sketched_by, post.sketchedBy)
            }
            binding.descriptionItem.apply {
                if (post.description.isNullOrEmpty())
                    visibility = View.GONE
                else
                    text = post.description
            }
            binding.timestampPostItem.text =
                DateFormat.getDateInstance(DateFormat.SHORT)
                    .format(Date(post.postTimestamp.toLong()))

            Glide.with(binding.photoItem.context)
                .load(post.imagePath.toUri())
                .format(DecodeFormat.PREFER_RGB_565)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_baseline_settings_24)
                .into(binding.photoItem)

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

        holder.bind(getItem(position), nickName)

    }


    class PostRemoteDiffCallback : DiffUtil.ItemCallback<PostRemote>() {
        override fun areItemsTheSame(oldItem: PostRemote, newItem: PostRemote): Boolean {
            return oldItem.postTimestamp == newItem.postTimestamp
        }

        override fun areContentsTheSame(oldItem: PostRemote, newItem: PostRemote): Boolean {
            return oldItem == newItem
        }

    }
}