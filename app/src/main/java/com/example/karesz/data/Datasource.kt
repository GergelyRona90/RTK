package com.example.karesz.data

import android.net.Uri
import android.util.Log
import java.io.File


class Datasource() {
    fun loadFolders(f: File): MutableList<String> {
        val prjDirectories = mutableListOf<String>()
        if (f.isDirectory) {
            val subDirectories = f.listFiles { file -> file.isDirectory }
            for (subDirector in subDirectories) {
                prjDirectories.add(subDirector.name).toString()
            }
        }
        return prjDirectories
    }


    fun loadJPGFilesNames(folder: File): MutableList<String> {
        val filesList = mutableListOf<String>()
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isFile && file.name.lowercase().endsWith(".jpg")) {
                    filesList.add(file.name)
                    Log.v("LoadJPGFilesnames-ből a file nevek:", file.name)
                }
            }
        }
        return filesList
    }
    fun loadJPGFiles(folder: File): MutableList<Uri> {
        val filesList = mutableListOf<Uri>()
        if (folder.exists() && folder.isDirectory) {
            folder.listFiles()?.forEach { file ->
                if (file.isFile && file.name.lowercase().endsWith(".jpg")) {
                    filesList.add(Uri.fromFile(file))
                }
            }
        }
        return filesList
    }
}