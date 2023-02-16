package com.example.artkeeper.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.artkeeper.data.ImageFilter
import com.example.artkeeper.databinding.ItemContainerFilterBinding
import com.example.artkeeper.utils.ImageFilterListener

class ImageFiltersAdapter(
    private val imageFilters: List<ImageFilter>,
    private val imageFilterListener: ImageFilterListener
) :
    RecyclerView.Adapter<ImageFiltersAdapter.ImageFilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFilterViewHolder {
        return ImageFilterViewHolder(
            ItemContainerFilterBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageFilterViewHolder, position: Int) {
        with(holder) {
            with(imageFilters[position]) {
                Glide.with(binding.imageFilterPreview)
                    .load(filterPreview)
                    .into(binding.imageFilterPreview)


                binding.textFilterName.text = name
                binding.root.setOnClickListener {
                    imageFilterListener.onFilterSelected(this)
                }
            }
        }
    }

    override fun getItemCount(): Int = imageFilters.size

    class ImageFilterViewHolder(val binding: ItemContainerFilterBinding) :
        RecyclerView.ViewHolder(binding.root)
}