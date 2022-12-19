package com.example.artkeeper.utils

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromUriToString(imagePath: Uri): String {
        return imagePath.toString()
    }

    @TypeConverter
    fun fromStringToUri(imagePath: String): Uri {
        return imagePath.toUri()
    }

    @TypeConverter
    fun fromListToString(nameChild: List<String>?): String? {
        return Gson().toJson(nameChild)
    }

    @TypeConverter
    fun fromStringToList(value: String?): List<String>? {
        val listType = object :
            TypeToken<List<String?>?>() {}.type
        return Gson()
            .fromJson<List<String>>(value, listType)
    }
/*
    @TypeConverter
    fun fromBitmapToByteArray(image: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun fromByteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
 */
}