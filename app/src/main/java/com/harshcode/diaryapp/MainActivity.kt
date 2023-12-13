package com.harshcode.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.harshcode.diaryapp.data.database.ImageToDeleteDAO
import com.harshcode.diaryapp.data.database.ImageToUploadDAO
import com.harshcode.diaryapp.navigation.Screen
import com.harshcode.diaryapp.navigation.SetUpNavGraph
import com.harshcode.diaryapp.ui.theme.DiaryAppTheme
import com.harshcode.diaryapp.util.Constants.APP_ID
import com.harshcode.diaryapp.util.retryDeletingImageToFirebase
import com.harshcode.diaryapp.util.retryUploadingImageToFirebase
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageToUploadDAO: ImageToUploadDAO

    @Inject
    lateinit var imageToDeleteDAO: ImageToDeleteDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetUpNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController
                )
            }
        }

        cleanupCheck(
            scope = lifecycleScope,
            imageToUploadDAO = imageToUploadDAO,
            imageToDeleteDAO = imageToDeleteDAO

        )
    }
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imageToUploadDAO: ImageToUploadDAO,
    imageToDeleteDAO: ImageToDeleteDAO
) {
    scope.launch(Dispatchers.IO) {
        val result = imageToUploadDAO.getAllImages()
        result.forEach { imageToUpload ->
            retryUploadingImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDAO.cleanUpImage(imageId = imageToUpload.id)
                    }
                }
            )
        }

        val result2 = imageToDeleteDAO.getAllImages()
        result2.forEach { imageToDelete ->
            retryDeletingImageToFirebase(
                imageToDelete = imageToDelete,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToDeleteDAO.cleanUpImage(imageToDelete.id)
                    }
                }
            )
        }
    }
}

fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route
    else Screen.Authentication.route
}