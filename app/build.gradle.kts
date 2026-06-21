import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}


android {
    namespace = "com.example.cinderssoul"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cinderssoul"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        val backendEnv = rootProject.file("backend/.env")
        val envProps = Properties().apply {
            if (backendEnv.exists()) {
                backendEnv.inputStream().use { load(it) }
            }
        }
        val googleServerClientId = providers.gradleProperty("GOOGLE_SERVER_CLIENT_ID").orNull
            ?: providers.environmentVariable("GOOGLE_SERVER_CLIENT_ID").orNull
            ?: envProps.getProperty("GOOGLE_SERVER_CLIENT_ID")
            ?: envProps.getProperty("GOOGLE_CLIENT_ID")
            ?: ""
        val shareBaseUrl = providers.gradleProperty("SHARE_BASE_URL").orNull
            ?: providers.environmentVariable("SHARE_BASE_URL").orNull
            ?: envProps.getProperty("SHARE_BASE_URL")
            ?: envProps.getProperty("APP_PUBLIC_URL")
            ?: ""
        val backendBaseUrl = providers.gradleProperty("BACKEND_BASE_URL").orNull
            ?: providers.environmentVariable("BACKEND_BASE_URL").orNull
            ?: envProps.getProperty("BACKEND_BASE_URL")
            ?: ""
        buildConfigField("String", "BASE_URL", "\"${backendBaseUrl.replace("\"", "\\\"")}\"")
        buildConfigField("String", "SHARE_BASE_URL", "\"${shareBaseUrl.replace("\"", "\\\"")}\"")
        buildConfigField(
            "String",
            "GOOGLE_SERVER_CLIENT_ID",
            "\"${googleServerClientId.replace("\"", "\\\"")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // implementation("com.google.firebase:firebase-analytics")
    // implementation("com.google.firebase:firebase-firestore")
    // implementation("com.google.firebase:firebase-auth")
    // implementation("com.google.firebase:firebase-storage")
    // implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Retrofit for REST API calls to Node.js backend
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation(libs.androidx.media3.session)

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Room Database (cho offline caching)
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Media3 ExoPlayer (để phát nhạc)
    val media3Version = "1.5.0"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-datasource:$media3Version")
    implementation("androidx.media3:media3-database:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media:media:1.7.0")

    // Coil (load ảnh từ URL)
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    implementation("io.coil-kt.coil3:coil-gif:3.0.4")

    // Gson (parse JSON)
    implementation("com.google.code.gson:gson:2.11.0")

    // OkHttp (networking)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //Blur image
    implementation(libs.blurview)
}
