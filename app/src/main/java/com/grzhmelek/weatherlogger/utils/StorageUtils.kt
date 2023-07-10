package com.grzhmelek.weatherlogger.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.grzhmelek.weatherlogger.BuildConfig
import com.grzhmelek.weatherlogger.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Store image before sharing
 * */
suspend fun storeImage(context: Context, imageData: Bitmap, fileName: String): Uri {
    var contentUri = Uri.EMPTY
    withContext(Dispatchers.IO) {
        // get path to external storage (SD card)
        val sdIconStorageDir: File?
        sdIconStorageDir = getAppDirectory(context)
        // create storage directories, if they don't exist
        if (!sdIconStorageDir.exists()) {
            sdIconStorageDir.mkdirs()
        }
        val filePath: String
        try {
            filePath = sdIconStorageDir.toString() + File.separator + fileName
            val fileOutputStream = FileOutputStream(filePath)
            val bos = BufferedOutputStream(fileOutputStream)
            imageData.compress(Bitmap.CompressFormat.JPEG, 80, bos)
            bos.flush()
            bos.close()
            galleryAddPic(context, filePath)
        } catch (e: FileNotFoundException) {
            Timber.e("FileNotFoundException: ${e.message}")
            return@withContext
        } catch (e: IOException) {
            Timber.e("IOException: ${e.message}")
            return@withContext
        }
        contentUri = getContentUri(context, filePath)
    }
    return contentUri
}

fun galleryAddPic(context: Context, filePath: String) {
    val mediaScanIntent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
    val contentUri: Uri = Uri.fromFile(File(filePath))
    mediaScanIntent.data = contentUri
    context.sendBroadcast(mediaScanIntent)
}

fun getAppDirectory(context: Context): File {
    return File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .absolutePath + "/" + context.getString(R.string.app_name) + "/"
    )
}

fun getContentUri(context: Context, filePath: String): Uri {
    return FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID + ".provider",
        File(filePath)
    )
}