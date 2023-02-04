package com.example.artkeeper.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
    var menu: Int = 0

    class PostItemViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            post: Post,
            nickName: String,
            clickListener: PostListener,
            menu: Int
        ) {
            binding.apply {
                nickNameItem.text = nickName
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
                    val popupMenu = PopupMenu(
                        it.context,
                        it
                    )
                    popupMenu.inflate(menu)

                    popupMenu.setOnMenuItemClickListener(object :
                        PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(item: MenuItem?): Boolean {
                            when (item?.itemId) {
                                R.id.cancel_button -> {
                                    clickListener.clickListener(post, adapterPosition, "remove")
                                    return true
                                }
                                R.id.share_post -> {
                                    clickListener.clickListener(post, adapterPosition, "share")
                                    return true
                                }
                            }
                            return false
                        }
                    })
                    popupMenu.show()
                }

            }

        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostAdapter.PostItemViewHolder {
        return PostAdapter.PostItemViewHolder(
            PostItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: PostAdapter.PostItemViewHolder,
        position: Int
    ) {
        Log.d("PostAdapter", position.toString())
        holder.bind(getItem(position), nickName, clickListener, menu)
    }

    class PostListener(val clickListener: (post: Post, position: Int, option: String) -> Unit)

    class PostRemoteDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            //return oldItem.id == newItem.id
            return oldItem.idPost == newItem.idPost
        }

        override fun areContentsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem == newItem
        }

    }
}