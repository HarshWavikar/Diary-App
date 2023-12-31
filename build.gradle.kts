
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.4.0")
    }
}
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false

    id("com.google.dagger.hilt.android") version "2.47" apply false
    id ("io.realm.kotlin") version "1.11.0" apply false
    id ("com.google.gms.google-services") version "4.3.15" apply false
}