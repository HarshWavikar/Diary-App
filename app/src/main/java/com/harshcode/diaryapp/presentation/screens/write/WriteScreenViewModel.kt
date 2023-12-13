package com.harshcode.diaryapp.presentation.screens.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.harshcode.diaryapp.data.database.ImageToDeleteDAO
import com.harshcode.diaryapp.data.database.ImageToUploadDAO
import com.harshcode.diaryapp.data.database.entity.ImageToDelete
import com.harshcode.diaryapp.data.database.entity.ImageToUpload
import com.harshcode.diaryapp.data.repository.MongoDB
import com.harshcode.diaryapp.model.Diary
import com.harshcode.diaryapp.model.GalleryImage
import com.harshcode.diaryapp.model.GalleryState
import com.harshcode.diaryapp.model.Mood
import com.harshcode.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.harshcode.diaryapp.util.RequestState
import com.harshcode.diaryapp.util.fetchImagesFromFirebase
import com.harshcode.diaryapp.util.toRealmInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDAO: ImageToUploadDAO,
    private val imageToDeleteDAO: ImageToDeleteDAO
) : ViewModel() {
    val galleryState = GalleryState()
    var uiState by mutableStateOf(UiState())
        private set


    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
        Log.d("DiaryId", "${uiState.selectedDiaryId}")
    }

    private fun fetchSelectedDiary() {
        if (uiState.selectedDiaryId != null) {
            viewModelScope.launch {
                MongoDB.getSelectedDiary(diaryId = ObjectId.invoke(uiState.selectedDiaryId!!))
                    .catch {
                        emit(RequestState.Error(Exception("Diary is already deleted")))
                    }
                    .collect { diary ->
                        if (diary is RequestState.Success) {
                            setSelectedDiary(diary = diary.data)
                            setTitle(title = diary.data.title)
                            setDescription(description = diary.data.description)
                            setMood(mood = Mood.valueOf(diary.data.mood))

                            fetchImagesFromFirebase(
                                remoteImagePaths = diary.data.images,
                                onImageDownload = { downloadedImage ->
                                    galleryState.addImage(
                                        GalleryImage(
                                            image = downloadedImage,
                                            remoteImagePath = extractImagePath(
                                                fullImageURL = downloadedImage.toString()
                                            ),
                                        )
                                    )
                                }
                            )
                        }
                    }
            }
        }
    }

    private fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun updatedDateTime(zonedDateTime: ZonedDateTime) {
        uiState = uiState.copy(updatedDateAndTime = zonedDateTime.toInstant().toRealmInstant())
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (uiState.selectedDiaryId != null) {
                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            } else {
                addNewDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }
        }
    }

    // INSERT DIARY
    private suspend fun addNewDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDB.insertDiary(diary = diary.apply {
            if (uiState.updatedDateAndTime != null) {
                date = uiState.updatedDateAndTime!!
            }
        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    // UPDATE DIARY
    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDB.updateDiary(diary = diary.apply {
            _id = ObjectId.invoke(uiState.selectedDiaryId!!)
            date = if (uiState.updatedDateAndTime != null) {
                uiState.updatedDateAndTime!!
            } else {
                uiState.selectedDiary!!.date
            }
        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            deleteImagesFromFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null) {
                val result = MongoDB.deleteDiary(ObjectId.invoke(uiState.selectedDiaryId!!))
                if (result is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        uiState.selectedDiary?.let { deleteImagesFromFirebase(images = it.images) }
                        onSuccess()
                    }
                } else if (result is RequestState.Error) {
                    withContext(Dispatchers.Main) {
                        onError(result.error.message.toString())
                    }
                }
            }
        }
    }

    fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDAO.addImageToDelete(
                                ImageToDelete(
                                    remoteImagePath = remotePath
                                )
                            )
                        }
                    }
            }
        } else {
            galleryState.imagesToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                viewModelScope.launch(Dispatchers.IO) {
                    imageToDeleteDAO.addImageToDelete(
                        ImageToDelete(
                            remoteImagePath = remotePath
                        )
                    )
                }
            }
        }
    }

    fun addImage(
        image: Uri,
        imageType: String
    ) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        Log.d("WriteViewModel", remoteImagePath)
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if (sessionUri != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDAO.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun extractImagePath(fullImageURL: String): String {
        val chucks = fullImageURL.split("%2F")
        val imageName = chucks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }
}


data class UiState(
    val selectedDiaryId: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateAndTime: RealmInstant? = null
)