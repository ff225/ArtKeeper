package com.example.artkeeper.utils

import com.example.artkeeper.data.model.ImageFilter

interface ImageFilterListener {
    fun onFilterSelected(imageFilter: ImageFilter)
}