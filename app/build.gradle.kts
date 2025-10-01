plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.romangolovanov.apps.ametro"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.romangolovanov.apps.ametro"
        minSdk = 24
        targetSdk = 36
        versionCode = 53
        versionName = "3.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName", "$applicationId-$versionName")
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.core)
    implementation(libs.loader)

    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.20.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")

    implementation("com.caverock:androidsvg:1.4")
}