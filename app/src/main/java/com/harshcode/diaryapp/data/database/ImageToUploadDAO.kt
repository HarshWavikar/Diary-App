package com.harshcode.diaryapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harshcode.diaryapp.data.database.entity.ImageToUpload

@Dao
interface ImageToUploadDAO {

    @Query("SELECT * FROM image_to_upload_table ORDER BY id ASC")
    suspend fun getAllImages(): List<ImageToUpload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToUpload(imagesToUpload: ImageToUpload)

    @Query("DELETE FROM image_to_upload_table WHERE id=:imageId ")
    suspend fun cleanUpImage(imageId: Int)
}