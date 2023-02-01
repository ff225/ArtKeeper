package com.example.artkeeper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.artkeeper.data.model.Nickname
import com.example.artkeeper.databinding.NicknameItemBinding

class HomeAdapter(private val clickListener: HomeListener) :
    ListAdapter<Nickname, HomeAdapter.HomeItemViewHolder>(HomeDiffCallback()) {
    class HomeItemViewHolder(private val binding: NicknameItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(nickname: Nickname, clickListener: HomeListener) {
            binding.nicknameTextView.text = nickname.nickName
            binding.nicknameCardView.setOnClickListener {
                clickListener.clickListener(nickname, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
        return HomeItemViewHolder(
            NicknameItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class HomeListener(val clickListener: (nickname: Nickname, position: Int) -> Unit)
}

class HomeDiffCallback : DiffUtil.ItemCallback<Nickname>() {
    override fun areItemsTheSame(oldItem: Nickname, newItem: Nickname): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: Nickname, newItem: Nickname): Boolean {
        return oldItem == newItem
    }

}