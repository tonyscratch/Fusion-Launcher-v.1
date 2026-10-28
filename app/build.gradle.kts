plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.fusion.launcher"
    compileSdk = 35
    defaultConfig { applicationId = "com.fusion.launcher"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.9.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
}
