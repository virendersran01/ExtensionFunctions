import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    //kotlin("kapt")
    //alias(libs.plugins.compose)
    id ("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    alias(libs.plugins.kotlin.serialization)
}


android {
    namespace = "com.virtualstudios.extensionfunctions"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.virtualstudios.extensionfunctions"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        val date = LocalDate.now()
        val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd"))
        setProperty("archivesBaseName", "VSTest_$formattedDate")

        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("config") {
            keyAlias = "alias"
            keyPassword = "password"
            storePassword = "password"
            storeFile = file("keystore.jks")
        }
        // Important: change the keystore for a production deployment
       /* val userKeystore = File(System.getProperty("user.home"), ".android/debug.keystore")
        val localKeystore = rootProject.file("debug_2.keystore")
        val hasKeyInfo = userKeystore.exists()
        create("release") {
            storeFile = if (hasKeyInfo) userKeystore else localKeystore
            storePassword = if (hasKeyInfo) "android" else System.getenv("compose_store_password")
            keyAlias = if (hasKeyInfo) "androiddebugkey" else System.getenv("compose_key_alias")
            keyPassword = if (hasKeyInfo) "android" else System.getenv("compose_key_password")
        }*/
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("config")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            isMinifyEnabled = true
            //signingConfig = signingConfigs.getByName("release")
            signingConfig = signingConfigs.getByName("config")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }

    }
    compileOptions {
        isCoreLibraryDesugaringEnabled  = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    /*kapt {
        correctErrorTypes = true
    }*/

//    kotlin {
//        jvmToolchain(11)
//    }

    packaging.resources {
        // Multiple dependency bring these files in. Exclude them to enable
        // our test APK to build (has no effect on our AARs)
        excludes += "/META-INF/AL2.0"
        excludes += "/META-INF/LGPL2.1"
    }
}

composeCompiler {
    //enableStrongSkippingMode = true
}
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.ccp)
    implementation(libs.libphonenumber)
    implementation(libs.pinview)
    implementation(libs.android.image.cropper)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //Lifecycle
//    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    //Image Loading
    implementation(libs.glide)
//    kapt(libs.glide.compiler)
    ksp(libs.glide.compiler)

    //Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    //Maps
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.google.maps.services)
    implementation(libs.places.ktx)
    implementation(libs.volley)

    //Dagger Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //Splash Screen
    implementation(libs.splash.screen)

    //kapt (libs.kotlinx.metadata.jvm)

    //preference
    implementation ("androidx.preference:preference-ktx:1.2.1")


//    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
//    testImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
//    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
//
//    implementation ("androidx.compose.runtime:runtime")
//    implementation ("androidx.compose.ui:ui")
//    implementation ("androidx.compose.foundation:foundation-layout")
//    implementation ("androidx.compose.material:material")
//    implementation ("androidx.compose.material:material-icons-extended")
//    implementation ("androidx.compose.foundation:foundation")
//    implementation ("androidx.compose.animation:animation")
//    implementation ("androidx.compose.ui:ui-tooling-preview")
//    implementation ("androidx.compose.runtime:runtime-livedata")
//    debugImplementation ("androidx.compose.ui:ui-tooling")
//    debugImplementation ("androidx.compose.ui:ui-test-manifest")
//    testImplementation ("androidx.compose.ui:ui-test-junit4")
//    androidTestImplementation ("androidx.compose.ui:ui-test")
//    androidTestImplementation ("androidx.compose.ui:ui-test-junit4")
//
//    implementation ("com.google.accompanist:accompanist-swiperefresh:0.34.0")
//    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
//
//    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
//
//    implementation ("androidx.navigation:navigation-compose:2.7.7")
//    implementation ("androidx.appcompat:appcompat:1.7.0")
//    implementation ("androidx.activity:activity-ktx:1.9.0")
//    implementation ("androidx.core:core-ktx:1.13.1")
//    implementation ("androidx.activity:activity-compose:1.9.0")
//
//    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
//    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.2")
//    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.compose.ui.viewbinding)
    implementation(libs.androidx.compose.ui.googlefonts)

    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)

    implementation(libs.bundles.koin)
    implementation(libs.bundles.ktor)
}

