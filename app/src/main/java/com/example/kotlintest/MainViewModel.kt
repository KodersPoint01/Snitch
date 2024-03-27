package com.example.kotlintest

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*


class MainViewModel : ViewModel() {
    val IMAGE_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val VIDEO_URI: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val AUDIO_URI: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val listImage = MutableLiveData<List<String>>()
    private val listAudio = MutableLiveData<List<String>>()
    private val listVideo = MutableLiveData<List<String>>()
    private val listApps = MutableLiveData<List<String>>()
    private val listDocuments = MutableLiveData<List<String>>()
    val docList: ArrayList<String> = ArrayList()
    var totalSize = 0L

    @SuppressLint("Range")
    fun getAllImage(cr: ContentResolver): MutableLiveData<List<String>> {
        val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE)
        val sortOrder = MediaStore.Images.Media._ID + " desc"
        val cur: Cursor? = cr.query(IMAGE_URI, projection, null, null, sortOrder)
        var pathNameList = ArrayList<String>()
        var totalSize = 0L
        if (cur != null) {
            pathNameList = ArrayList()
            //  val sizeColumn = cur.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            while (cur.moveToNext()) {
                val data = cur
                //  Log.d("TAG", "getAllImage: $data")
                val fileName: String = cur.getString(0)
                try {
                    val sizeC: Long =
                        cur.getString(cur.getColumnIndex(MediaStore.Images.Media.SIZE)).toLong()
                    totalSize = totalSize + sizeC
                } catch (e: Exception) {
                }

                /*       Log.d("TAG", "getAllImage:  size of image $sizeC")
                       Log.d("TAG", "getAllImage:  size of image $totalSize")*/
                //val size = cur.getString(MediaStore.Images.Media.SIZE)
                // Log.d("TAG", "getAllImage:image size $size")
                //     Log.d("TAG", "getAllImage: name$fileName")
                pathNameList.add(fileName)
            }
            MainActivity.totalImageSize = totalSize
            Log.d("TAG", "getAllImage: imagesTotal size model ${totalSize}")
            cur.close()
        }
        listImage.value = pathNameList
        Log.d("TAG", "getAllImage: size ${pathNameList.size}")
        return listImage
    }

    @SuppressLint("Range")
    fun getVideo(cr: ContentResolver): MutableLiveData<List<String>> {
        val projection = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE)
        val sortOrder = MediaStore.Video.Media._ID + " desc"
        val cur: Cursor? = cr.query(VIDEO_URI, projection, null, null, sortOrder)
        var pathNameList = ArrayList<String>()
        var totalSize = 0L
        if (cur != null) {
            pathNameList = ArrayList()
            //  val sizeColumn = cur.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            while (cur.moveToNext()) {
                val data = cur
                //  Log.d("TAG", "getAllImage: $data")
                val fileName: String = cur.getString(0)
                try {
                    val sizeC: Long =
                        cur.getString(cur.getColumnIndex(MediaStore.Video.Media.SIZE)).toLong()
                    totalSize = totalSize + sizeC
                } catch (e: Exception) {
                }
                //val size = cur.getString(MediaStore.Images.Media.SIZE)
                // Log.d("TAG", "getAllImage:image size $size")
                //  Log.d("TAG", "getAllImage: name$fileName")
                pathNameList.add(fileName)
            }
            MainActivity.totalVideoSize = totalSize
            cur.close()
        }
        listVideo.value = pathNameList
        Log.d("TAG", "getAllVideo: size ${pathNameList.size}")
        return listVideo
    }

    @SuppressLint("Range")
    fun getAudio(cr: ContentResolver): MutableLiveData<List<String>> {
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.SIZE)
        val sortOrder = MediaStore.Audio.Media._ID + " desc"
        val cur: Cursor? = cr.query(AUDIO_URI, projection, null, null, sortOrder)
        var pathNameList = ArrayList<String>()
        var totalSize = 0L
        if (cur != null) {
            pathNameList = ArrayList()
            //  val sizeColumn = cur.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            while (cur.moveToNext()) {
                val data = cur
                //  Log.d("TAG", "getAllImage: $data")
                val fileName: String = cur.getString(0)
                try {
                    val sizeC: Long =
                        cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE)).toLong()
                    totalSize = totalSize + sizeC
                } catch (e: Exception) {
                }
                //val size = cur.getString(MediaStore.Images.Media.SIZE)
                // Log.d("TAG", "getAllImage:image size $size")
                //  Log.d("TAG", "getAllImage: name$fileName")
                pathNameList.add(fileName)
            }
            MainActivity.totalAudioSize = totalSize
            cur.close()
        }
        listAudio.value = pathNameList
        Log.d("TAG", "getAudio: size ${pathNameList.size}")
        return listAudio
    }

    fun getApps(context: Context): MutableLiveData<List<String>> {
        var pathNameList = ArrayList<String>()
        val apps: List<ApplicationInfo> = context.packageManager.getInstalledApplications(0)
        var totalSize = 0L
        for (app in apps) {
            if (app.flags and (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) > 0) {
                // It is a system app
            } else {
                //  Log.d("TAG", "getApps: ${app.name}${app.packageName}")
                pathNameList.add(app.packageName)
                val sizeC: Long =
                    app.packageName.length.toLong()
                Log.d("TAG", "getApps:size $sizeC ")
                totalSize = totalSize + sizeC
            }
        }
        MainActivity.totalAppsSize = totalSize
        listApps.value = pathNameList
        return listApps
    }


    @SuppressLint("Range")
    fun getPdfList(contentResolver: ContentResolver): MutableLiveData<List<String>> {
        val pdfList: ArrayList<String> = ArrayList()
        val collection: Uri
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgs = arrayOf(mimeType)
        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        docList.add(cursor.getString(columnData))
                        try {
                            val sizeC: Long =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                                    .toLong()
                            totalSize = totalSize + sizeC
                        } catch (e: Exception) {
                        }
                        // Log.d("TAG", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }

        //  listDocuments.value = pdfList
        return getDocsList(contentResolver)
    }

    @SuppressLint("Range")
    fun getDocsList(contentResolver: ContentResolver): MutableLiveData<List<String>> {
        val collection: Uri
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")
        val selectionArgs = arrayOf(mimeType2)

        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        docList.add(cursor.getString(columnData))
                        try {
                            val sizeC: Long =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                                    .toLong()
                            totalSize = totalSize + sizeC
                        } catch (e: Exception) {
                        }
                        //Log.d("TAG", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }

        return getDocList(contentResolver)
    }

    @SuppressLint("Range")
    fun getDocList(contentResolver: ContentResolver): MutableLiveData<List<String>> {
        val collection: Uri
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Audio.Media.SIZE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val mimeType2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
        val mimeType3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
        val mimeType4 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")
        val selectionArgs = arrayOf(mimeType2)
        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        docList.add(cursor.getString(columnData))
                        try {
                            val sizeC: Long =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                                    .toLong()
                            totalSize = totalSize + sizeC
                        } catch (e: Exception) {
                        }
                        //  Log.d("TAG", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        //   listDocuments.value = docList
        return getxlsList(contentResolver)
    }

    @SuppressLint("Range")
    fun getxlsList(contentResolver: ContentResolver): MutableLiveData<List<String>> {
        val pdfList: ArrayList<String> = ArrayList()
        val collection: Uri
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE, MediaStore.Audio.Media.SIZE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val mimeType2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
        val mimeType3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
        val mimeType4 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")
        val selectionArgs = arrayOf(mimeType4)
        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        docList.add(cursor.getString(columnData))
                        try {
                            val sizeC: Long =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                                    .toLong()
                            totalSize = totalSize + sizeC
                        } catch (e: Exception) {
                        }
                        //   Log.d("TAG", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        // listDocuments.value = pdfList

        return gettxtList(contentResolver)
    }

    @SuppressLint("Range")
    fun gettxtList(contentResolver: ContentResolver): MutableLiveData<List<String>> {
        val pdfList: ArrayList<String> = ArrayList()
        val collection: Uri
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val mimeType2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")
        val mimeType3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")
        val mimeType4 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")
        val selectionArgs = arrayOf(mimeType3)
        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        docList.add(cursor.getString(columnData))
                        try {
                            val sizeC: Long =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                                    .toLong()
                            totalSize = totalSize + sizeC
                        } catch (e: Exception) {
                        }
                        // Log.d("TAG", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        //  listDocuments.value = pdfList
        return getZipList(contentResolver)
    }

    @SuppressLint("Range")
    fun getZipList(contentResolver: ContentResolver): MutableLiveData<List<String>> {
        val pdfList: ArrayList<String> = ArrayList()
        val collection: Uri
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )
        val sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val mimeType4 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("zip")
        val selectionArgs = arrayOf(mimeType4)
        collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)
            .use { cursor ->
                assert(cursor != null)
                if (cursor!!.moveToFirst()) {
                    val columnData: Int = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val columnName: Int =
                        cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    do {
                        pdfList.add(cursor.getString(columnData))
                        docList.add(cursor.getString(columnData))
                        try {
                            val sizeC: Long =
                                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                                    .toLong()
                            totalSize = totalSize + sizeC
                        } catch (e: Exception) {
                        }
                        // Log.d("TAG", "getPdf: " + cursor.getString(columnData))
                        //you can get your pdf files
                    } while (cursor.moveToNext())
                }
            }
        MainActivity.totalDocumentsSize = totalSize
        listDocuments.value = docList
        return listDocuments
    }
}