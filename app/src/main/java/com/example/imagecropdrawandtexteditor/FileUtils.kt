package com.example.imagecropdrawandtexteditor

import android.content.Context
import android.os.Environment

import android.os.Build
import java.io.File



object FileUtils {
    const val FOLDER_NAME = "MyImageEditor"


    fun createFolders(): File {
        val baseDir: File?
        baseDir = if (Build.VERSION.SDK_INT < 8) {
            Environment.getExternalStorageDirectory()
        } else {
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        }
        if (baseDir == null) return Environment.getExternalStorageDirectory()
        val aviaryFolder = File(baseDir, FOLDER_NAME)
        if (aviaryFolder.exists()) return aviaryFolder
        if (aviaryFolder.isFile) aviaryFolder.delete()
        return if (aviaryFolder.mkdirs()) aviaryFolder else Environment.getExternalStorageDirectory()
    }

    @JvmStatic
	fun genEditFile(): File? {
        return getEmptyFile("image"
                + System.currentTimeMillis() + ".png")
    }

    fun getEmptyFile(name: String?): File? {
        val folder = createFolders()
        if (folder != null) {
            if (folder.exists()) {
                return File(folder, name)
            }
        }
        return null
    }










}