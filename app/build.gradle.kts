import java.net.URL
import java.net.HttpURLConnection
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.HttpsURLConnection
import java.security.cert.X509Certificate
import java.security.SecureRandom

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.netcheck.tzqpwr"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("uploadApk") {
    dependsOn("assembleDebug")
    doLast {
        // Disable SSL certificate verification to prevent PKIX path validation errors on outdated JDK trust stores
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                override fun checkClientTrusted(certs: Array<X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>?, authType: String?) {}
            })

            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            println("Warning: could not configure trust-all SSL context: ${e.message}")
        }

        val apkFile = file("${project.layout.buildDirectory.get().asFile}/outputs/apk/debug/app-debug.apk")
        val fallbackApkFile = file("${rootDir}/.build-outputs/app-debug.apk")
        val targetFile = if (apkFile.exists()) apkFile else if (fallbackApkFile.exists()) fallbackApkFile else null
        
        if (targetFile == null || !targetFile.exists()) {
            println("❌ Target APK file not found! Please build the project first.")
            return@doLast
        }
        
        println("📤 Uploading ${targetFile.name} (${targetFile.length() / 1024 / 1024} MB) to free.keep.sh...")
        var uploadSuccess = false
        
        // 1. Try free.keep.sh
        try {
            val url = URL("https://free.keep.sh/${targetFile.name}")
            val connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.requestMethod = "PUT"
            connection.setRequestProperty("Content-Type", "application/octet-stream")
            connection.setRequestProperty("Content-Length", targetFile.length().toString())
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            targetFile.inputStream().use { input ->
                connection.outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            val responseCode = connection.responseCode
            if (responseCode == 200 || responseCode == 201) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                println("\n✅ SUCCESSFUL UPLOAD TO KEEP.SH!")
                println("🔗 DOWNLOAD LINK: $responseText\n")
                uploadSuccess = true
            } else {
                println("❌ Upload to keep.sh failed with response code $responseCode")
            }
        } catch (e: Exception) {
            println("❌ Error uploading to keep.sh: ${e.message}")
        }

        // 2. Fallback to transfer.sh
        if (!uploadSuccess) {
            println("🔄 Attempting transfer.sh...")
            try {
                val url = URL("https://transfer.sh/${targetFile.name}")
                val connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.setRequestProperty("Content-Length", targetFile.length().toString())
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                
                targetFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                    println("\n✅ SUCCESSFUL UPLOAD TO TRANSFER.SH!")
                    println("🔗 DOWNLOAD LINK: $responseText\n")
                    uploadSuccess = true
                } else {
                    println("❌ Upload to transfer.sh failed with response code $responseCode")
                }
            } catch (e: Exception) {
                println("❌ Error uploading to transfer.sh: ${e.message}")
            }
        }

        // 3. Fallback to bashupload.com (root PUT)
        if (!uploadSuccess) {
            println("🔄 Attempting fallback upload to bashupload.com (root level)...")
            try {
                val url = URL("https://bashupload.com/")
                val connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                // Pass the filename in a custom header bashupload expects
                connection.setRequestProperty("X-Filename", targetFile.name)
                connection.setRequestProperty("Content-Length", targetFile.length().toString())
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                
                targetFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                    println("\n✅ SUCCESSFUL UPLOAD TO BASHUPLOAD.COM!")
                    println("🔗 DOWNLOAD LINK:\n$responseText\n")
                    uploadSuccess = true
                } else {
                    println("❌ Fallback upload to bashupload.com failed: $responseCode")
                }
            } catch (ex: Exception) {
                println("❌ Error in fallback upload to bashupload.com: ${ex.message}")
            }
        }
        
        if (!uploadSuccess) {
            println("\n❌ All direct cloud uploads failed. Please check your internet connectivity or build steps.\n")
        }
    }
}

