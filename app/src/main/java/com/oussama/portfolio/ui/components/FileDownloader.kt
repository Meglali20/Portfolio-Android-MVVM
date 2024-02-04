package com.oussama.portfolio.ui.components

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.oussama.portfolio.R
import java.net.URI
import java.net.URISyntaxException


class FileDownloader(
    private val context: Context
) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val fileName = extractFileNameFromUrl(url)
        val request = DownloadManager.Request(url.toUri())
            .setMimeType(getMimeType(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(String.format(context.getString(R.string.downloadingTitle), fileName))
            .addRequestHeader("Authorization", "Bearer <token>")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        return downloadManager.enqueue(request)
    }

    private fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun extractFileNameFromUrl(url: String): String {
        val uri = try {
            URI(url)
        } catch (e: URISyntaxException) {
            return ""
        }

        val path = uri.path
        val lastSlashIndex = path.lastIndexOf('/')

        return if (lastSlashIndex != -1 && lastSlashIndex < path.length - 1) {
            path.substring(lastSlashIndex + 1)
        } else {
            path
        }
    }
}