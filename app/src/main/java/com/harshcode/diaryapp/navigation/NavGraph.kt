package com.harshcode.diaryapp.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.harshcode.diaryapp.data.repository.MongoDB
import com.harshcode.diaryapp.model.GalleryImage
import com.harshcode.diaryapp.model.Mood
import com.harshcode.diaryapp.model.rememberGalleryState
import com.harshcode.diaryapp.presentation.components.DisplayAlertDialog
import com.harshcode.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.harshcode.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.harshcode.diaryapp.presentation.screens.home.HomeScreen
import com.harshcode.diaryapp.presentation.screens.home.HomeScreenViewModel
import com.harshcode.diaryapp.presentation.screens.write.WriteScreen
import com.harshcode.diaryapp.presentation.screens.write.WriteScreenViewModel
import com.harshcode.diaryapp.util.Constants.APP_ID
import com.harshcode.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetUpNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        //Here we need to define all our screens that our application will have
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            }
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToAuthenticationScreen = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            navigateToWriteWithArgs = {
                navController.navigate(Screen.Write.passDiaryId(diaryId = it))
            }
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}


fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        val authenticated by viewModel.authenticated

        AuthenticationScreen(
            authenticated = authenticated,
            messageBarState = messageBarState,
            oneTapState = oneTapState,
            loadingState = loadingState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoading(false)
            },
            onDialogDismiss = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToAuthenticationScreen: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit
) {
    composable(route = Screen.Home.route) {
        val context = LocalContext.current
        val viewModel: HomeScreenViewModel = hiltViewModel()
        val diaries by viewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var signOutDialogOpened by remember { mutableStateOf(false) }
        var deleteAllDialogOpened by remember { mutableStateOf(false) }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            onDeleteAllClicked = {
                deleteAllDialogOpened = true
            },
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs,
            dateIsSelected = viewModel.dateIsSelected,
            onDateSelected = { viewModel.getDiaries(it) },
            onDateReset = { viewModel.getDiaries() }
        )

        LaunchedEffect(key1 = Unit) {
            MongoDB.configureTheRealm()
        }
        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from your Google Account?",
            dialogOpened = signOutDialogOpened,
            onCloseDialog = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuthenticationScreen()
                        }
                    }
                }
            }
        )

        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want to permanently delete all your diaries?",
            dialogOpened = deleteAllDialogOpened,
            onCloseDialog = { deleteAllDialogOpened = false },
            onYesClicked = {
                viewModel.deleteAllDiaries(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Successfully deleted all diaries.",
                            Toast.LENGTH_LONG
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        Toast.makeText(
                            context, if (it.message == "No internet connection.")
                                "We need internet Connection for this operation."
                            else it.message,
                            Toast.LENGTH_LONG
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed: () -> Unit
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteScreenViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val context = LocalContext.current
        val pagerState = rememberPagerState()
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }
        val galleryState = viewModel.galleryState
//        val galleryState = rememberGalleryState()

        LaunchedEffect(key1 = uiState) {
            Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
        }

        WriteScreen(
            uiState = uiState,
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            pagerState = pagerState,
            galleryState = galleryState,
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Diary deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onBackPressed = onBackPressed,
            moodName = { Mood.values()[pageNumber].name },
            onSavedClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = {
                        onBackPressed()
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onDateTimeUpdated = { viewModel.updatedDateTime(zonedDateTime = it) },
            onImageSelect = {
                val type = context.contentResolver.getType(it)?.split("/")?.last() ?: "jpg"
                Log.d("WriteViewModel", "Uri: $it")
                viewModel.addImage(image = it, imageType = type)
            },
            onImageDeleteClicked = {
                galleryState.removeImage(it)
            }
        )
    }
}