package com.cindaku.holanear.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils


class FileHelper {
    companion object{
        fun processUri(context: Context, uri: Uri): String? {
            val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            var path: String? = ""
            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                    val id = DocumentsContract.getDocumentId(uri)
                    //Starting with Android O, this "id" is not necessarily a long (row number),
                    //but might also be a "raw:/some/file/path" URL
                    if (id != null && id.startsWith("raw:/")) {
                        val rawuri: Uri = Uri.parse(id)
                        path = rawuri.getPath()
                    } else {
                        val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads"
                        )
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            val contentUri: Uri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id)
                            )
                            path = getDataColumn(context, contentUri, null, null)
                            if (!TextUtils.isEmpty(path)) {
                                break
                            }
                        }
                    }
                } else if (isMediaDocument(uri)) { // MediaProvider
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs: Array<String> = arrayOf(
                        split[1]
                    )
                    path = getDataColumn(context, contentUri, selection, selectionArgs as Array<String?>)
                } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {
                    path = getDataColumn(context, uri, null, null)
                }
            } else if ("content".equals(
                    uri.getScheme(),
                    ignoreCase = true
                )
            ) { // MediaStore (and general)
                path = getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.getScheme(), ignoreCase = true)) { // File
                path = uri.getPath()
            }
            return path
        }
        fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String?>?
        ): String? {
            var cursor: Cursor? = null
            var result: String? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    result = cursor.getString(index)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                return null
            } finally {
                cursor?.close()
            }
            return result
        }
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }
    }
}