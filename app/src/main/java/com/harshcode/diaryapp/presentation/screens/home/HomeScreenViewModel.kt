package com.harshcode.diaryapp.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.harshcode.diaryapp.connectivity.ConnectivityObserver
import com.harshcode.diaryapp.connectivity.NetworkConnectivityObserver
import com.harshcode.diaryapp.data.database.ImageToDeleteDAO
import com.harshcode.diaryapp.data.database.entity.ImageToDelete
import com.harshcode.diaryapp.data.repository.Diaries
import com.harshcode.diaryapp.data.repository.MongoDB
import com.harshcode.diaryapp.model.Diary
import com.harshcode.diaryapp.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imageToDeleteDAO: ImageToDeleteDAO
) : ViewModel() {
private lateinit var allDiariesJob: Job
private lateinit var filteredDiariesJob: Job

    private var network by mutableStateOf(ConnectivityObserver.Status.Unavailable)
    val diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    var dateIsSelected by mutableStateOf(false)

    init {
        getDiaries()
        viewModelScope.launch {
            connectivity.observe().collect {
                network = it
            }
        }
    }

    fun getDiaries(zonedDateTime: ZonedDateTime? = null) {
        dateIsSelected = zonedDateTime != null
        if (dateIsSelected && zonedDateTime != null) {
            observeFilteredDiaries(zonedDateTime = zonedDateTime)
        }else{
            observeAllDiaries()
        }
    }

    private fun observeAllDiaries() {
       allDiariesJob =  viewModelScope.launch {
           if (::filteredDiariesJob.isInitialized){
               filteredDiariesJob.cancelAndJoin()
           }
            MongoDB.getAllDiaries().collect { result ->
                diaries.value = result
            }
        }
    }

    private fun observeFilteredDiaries(zonedDateTime: ZonedDateTime) {
        filteredDiariesJob = viewModelScope.launch {
            if (::allDiariesJob.isInitialized){
                allDiariesJob.cancelAndJoin()
            }
            MongoDB.getFilteredDiaries(zonedDateTime = zonedDateTime).collect { result ->
                diaries.value = result
            }
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (network == ConnectivityObserver.Status.Available) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imagesDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imagesDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imageToDeleteDAO.addImageToDelete(
                                        ImageToDelete(
                                            remoteImagePath = imagePath
                                        )
                                    )
                                }
                            }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = MongoDB.deleteAllDiaries()
                        if (result is RequestState.Success) {
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else if (result is RequestState.Error) {
                            withContext(Dispatchers.Main) {
                                onError(result.error)
                            }
                        }
                    }
                }
                .addOnFailureListener { onError(it) }
        } else {
            onError(Exception("No internet connection."))
        }
    }
}