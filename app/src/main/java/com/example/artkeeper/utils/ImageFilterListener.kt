package com.example.artkeeper.utils

import com.example.artkeeper.data.ImageFilter

interface ImageFilterListener {
    fun onFilterSelected(imageFilter: ImageFilter)
}