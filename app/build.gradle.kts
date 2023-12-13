import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id ("com.google.gms.google-services") //This plugin is used for firebase, we are using firebase in our project to store images
}

android {
    namespace = "com.harshcode.diaryapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.harshcode.diaryapp"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    //Compose Navigation - This is the library that we are going to need to navigate between our composable screens
    implementation("androidx.navigation:navigation-compose:2.7.5")

    //Firebase
    implementation ("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")

    //Room
    implementation("androidx.room:room-runtime:2.6.1")
    //noinspection KaptUsageInsteadOfKsp
    kapt ("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    //Runtime Compose  - we going to need this library to access certain functionalities for jetpack compose
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    //Splash Api  - This dependency is to implement splash screen in our application, This is the newest api for splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    //Mongo DB Realm
    implementation ("io.realm.kotlin:library-sync:1.11.0")// If using Device Sync
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // If using coroutines with the SDK

    //Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    "kapt"("com.google.dagger:hilt-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    //Google Auth

    //Coil
//    implementation("io.coil-kt:coil:2.5.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    //Pager - Accompanist
    implementation("com.google.accompanist:accompanist-pager:0.27.0")

    //Date-Time picker
    implementation ("com.maxkeppeler.sheets-compose-dialogs:core:1.2.0")
    implementation ("com.maxkeppeler.sheets-compose-dialogs:calendar:1.2.0")   // CALENDAR
    implementation ("com.maxkeppeler.sheets-compose-dialogs:clock:1.2.0")      // CLOCK

    //Message bar Compose
    implementation ("com.github.stevdza-san:MessageBarCompose:1.0.8")

    //One Tap Compose
    implementation("com.github.stevdza-san:OneTapCompose:1.0.9")

    //Desugar JDK
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")
}




