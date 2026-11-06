plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // My added for firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.visioncoders.i220795_i210611"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.visioncoders.i220795_i210611"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // Unit testing
    testImplementation(libs.junit)

    // Instrumented testing (Espresso, JUnit, Rules)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")

    // My added for firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Agora SDK
    implementation("io.agora.rtc:full-sdk:4.2.6")

}