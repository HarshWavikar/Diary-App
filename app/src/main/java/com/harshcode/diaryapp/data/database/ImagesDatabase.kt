package com.harshcode.diaryapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.harshcode.diaryapp.data.database.entity.ImageToDelete
import com.harshcode.diaryapp.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun imageToUploadDAO() : ImageToUploadDAO
    abstract fun imageToDeleteDAO() : ImageToDeleteDAO
}