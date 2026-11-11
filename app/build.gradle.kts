plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.github.m4stas.technotunniplaan"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.m4stas.technotunniplaan"
        minSdk = 26
        targetSdk = 37
        versionCode = providers.gradleProperty("versionCode").orElse("1").get().toInt()
        versionName = providers.gradleProperty("versionName").orElse("0.1.0").get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    // Robolectric's SDK 37 runtime reads jdk.internal.access, which JDK 21 does not export by default.
    testOptions.unitTests.all { it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED") }
    buildTypes {
        release {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.work.runtime)
    implementation(libs.datastore.preferences)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.serialization.json)
    implementation(libs.coroutines.android)
    implementation(libs.okhttp)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.work.testing)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockwebserver3)
    testImplementation(libs.compose.ui.test.junit4)
    // Compose test pulls Espresso 3.5.0, which calls InputManager.getInstance(), gone on SDK 37.
    testImplementation(libs.espresso.core)
    debugImplementation(libs.compose.ui.test.manifest)
}
