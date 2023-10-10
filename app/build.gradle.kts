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
}


android {
    namespace = "com.virtualstudios.extensionfunctions"
    compileSdk  = 34

    defaultConfig {
        applicationId = "com.virtualstudios.extensionfunctions"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val date = LocalDate.now()
        val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MMM-dd"))
        setProperty("archivesBaseName", "VSTest_$formattedDate")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("config") {
            keyAlias = "alias"
            keyPassword = "password"
            storePassword = "password"
            storeFile = file("keystore.jks")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("config")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            signingConfig = signingConfigs.getByName("config")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    }

    /*kapt {
        correctErrorTypes = true
    }*/

//    kotlin {
//        jvmToolchain(11)
//    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.ccp)
    implementation(libs.libphonenumber)
    implementation(libs.pinview)
    implementation(libs.android.image.cropper)
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
}
