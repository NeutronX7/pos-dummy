plugins {
    id("com.android.library")
}

android {
    namespace = "com.example.sunmi"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("proguard-rules.pro")

        ndk {
            abiFilters += listOf("armeabi-v7a")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.printerx)

    implementation(files("libs/PayLib-release-2.0.41.aar"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(files("libs/sunmiemvl2split-1.0.3.jar"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
