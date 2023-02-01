package com.example.artkeeper.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.artkeeper.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class ShareAction {

    private suspend fun getPhoto(ctx: Context, imagePath: String): Bitmap {
        return withContext(Dispatchers.IO) {
            async {
                Glide.with(ctx).asBitmap().load(imagePath).submit().get()
            }
        }.await()
    }

    fun getTmpFileUri(ctx: Context, imagePath: String): Uri {
        return runBlocking {
            val bitmap = getPhoto(ctx, imagePath)
            val tmpFile =
                File.createTempFile("tmp_image_file", ".jpg", ctx.cacheDir).apply {
                    createNewFile()
                    deleteOnExit()
                }

            try {
                val stream: OutputStream = FileOutputStream(tmpFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val path = FileProvider.getUriForFile(
                ctx,
                "${BuildConfig.APPLICATION_ID}.file_provider",
                File(tmpFile.path)
            )

            Log.d(javaClass.simpleName, path.toString())
            return@runBlocking path
        }
    }
}