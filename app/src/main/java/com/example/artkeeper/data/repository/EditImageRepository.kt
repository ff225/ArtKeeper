package com.example.artkeeper.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.example.artkeeper.data.ImageFilter

interface EditImageRepository {

    suspend fun prepareImagePreview(imageUri: Uri): Bitmap?

    suspend fun getImageFilters(image: Bitmap): List<ImageFilter>

    suspend fun saveFilteredImage(filteredBitmap: Bitmap): Uri?
}