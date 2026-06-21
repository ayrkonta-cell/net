import java.net.URL
import java.net.HttpURLConnection
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.HttpsURLConnection
import java.security.cert.X509Certificate
import java.security.SecureRandom
import java.io.PrintWriter
import java.io.OutputStream
import java.nio.charset.StandardCharsets

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
    val buildDir = layout.buildDirectory.get().asFile
    val rootDirectory = rootDir
    val apkFile = file("${buildDir}/outputs/apk/debug/app-debug.apk")
    val fallbackApkFile = file("${rootDirectory}/.build-outputs/app-debug.apk")
    
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

        val targetFile = if (apkFile.exists()) apkFile else if (fallbackApkFile.exists()) fallbackApkFile else null
        
        if (targetFile == null || !targetFile.exists()) {
            println("❌ Target APK file not found! Please build the project first.")
            return@doLast
        }
        
        println("📤 Found target APK: ${targetFile.absolutePath} (${targetFile.length() / 1024 / 1024} MB)")
        var uploadSuccess = false

        // Unified Multipart Uploader Helper Function
        fun uploadMultipart(
            apiUrl: String,
            fileParamName: String,
            file: File,
            additionalParams: Map<String, String> = emptyMap()
        ): String? {
            val boundary = "===Boundary" + System.currentTimeMillis() + "==="
            val LINE_FEED = "\r\n"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doOutput = true
            connection.doInput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connectTimeout = 60000
            connection.readTimeout = 60000

            try {
                val os: OutputStream = connection.outputStream
                os.use { outputStream ->
                    val writer = PrintWriter(outputStream.writer(StandardCharsets.UTF_8), true)
                    
                    // Write additional text fields
                    additionalParams.forEach { (key, value) ->
                        writer.append("--$boundary").append(LINE_FEED)
                        writer.append("Content-Disposition: form-data; name=\"$key\"").append(LINE_FEED)
                        writer.append(LINE_FEED)
                        writer.append(value).append(LINE_FEED)
                    }

                    // Write file header
                    writer.append("--$boundary").append(LINE_FEED)
                    writer.append("Content-Disposition: form-data; name=\"$fileParamName\"; filename=\"${file.name}\"").append(LINE_FEED)
                    writer.append("Content-Type: application/vnd.android.package-archive").append(LINE_FEED)
                    writer.append(LINE_FEED)
                    writer.flush()

                    // Write file data
                    file.inputStream().use { input ->
                        input.copyTo(outputStream)
                    }
                    outputStream.flush()

                    // End of multipart form
                    writer.append(LINE_FEED)
                    writer.append("--$boundary--").append(LINE_FEED)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                return if (responseCode == 200 || responseCode == 201) {
                    connection.inputStream.bufferedReader().use { it.readText() }.trim()
                } else {
                    val err = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    println("⚠️ Server returned error code $responseCode: $err")
                    null
                }
            } catch (e: Exception) {
                println("⚠️ HTTP error during multipart upload to $apiUrl: ${e.message}")
                return null
            }
        }
        
        // 1. Try Litterbox.catbox.moe (multipart, temporary 3-day storage)
        println("🔄 Attempting upload to litterbox.catbox.moe (72h duration)...")
        try {
            val res = uploadMultipart(
                "https://litterbox.catbox.moe/resources/internals/api.php",
                "fileToUpload",
                targetFile,
                mapOf("reqtype" to "fileupload", "time" to "72h")
            )
            if (res != null && res.startsWith("http")) {
                println("\n========================================")
                println("🔗 EXTERNAL DOWNLOAD LINK (Litterbox):")
                println(res)
                println("========================================\n")
                uploadSuccess = true
            } else {
                println("⚠️ Litterbox upload failed: response was unacceptable or null.")
            }
        } catch (e: Exception) {
            println("⚠️ Litterbox upload error: ${e.message}")
        }

        // 2. Try Catbox.moe (multipart, unlimited download, stable)
        if (!uploadSuccess) {
            println("🔄 Attempting upload to catbox.moe (anonymous multi-download)...")
            try {
                val res = uploadMultipart(
                    "https://catbox.moe/user/api.php",
                    "fileToUpload",
                    targetFile,
                    mapOf("reqtype" to "fileupload")
                )
                if (res != null && res.startsWith("http")) {
                    println("\n========================================")
                    println("🔗 EXTERNAL DOWNLOAD LINK (Catbox):")
                    println(res)
                    println("========================================\n")
                    uploadSuccess = true
                } else {
                    println("⚠️ Catbox upload failed: response was unacceptable or null.")
                }
            } catch (e: Exception) {
                println("⚠️ Catbox upload error: ${e.message}")
            }
        }

        // 3. Try Pixeldrain (direct PUT upload)
        if (!uploadSuccess) {
            println("🔄 Attempting upload to pixeldrain.com (direct PUT)...")
            try {
                val url = URL("https://pixeldrain.com/api/file/${targetFile.name}")
                val connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.setRequestProperty("Content-Length", targetFile.length().toString())
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connectTimeout = 45000
                connection.readTimeout = 45000

                targetFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                val responseCode = connection.responseCode
                if (responseCode == 200 || responseCode == 201) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                    val idRegex = """"id"\s*:\s*"([^"]+)"""".toRegex()
                    val match = idRegex.find(responseText)
                    val fileId = match?.groupValues?.get(1)
                    if (fileId != null) {
                        val downloadLink = "https://pixeldrain.com/api/file/$fileId"
                        println("\n========================================")
                        println("🔗 EXTERNAL DOWNLOAD LINK (Pixeldrain):")
                        println(downloadLink)
                        println("========================================\n")
                        uploadSuccess = true
                    } else {
                        println("⚠️ Could not parse Pixeldrain success response: $responseText")
                    }
                } else {
                    val err = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    println("⚠️ Pixeldrain returned code $responseCode: $err")
                }
            } catch (e: Exception) {
                println("⚠️ Pixeldrain upload error: ${e.message}")
            }
        }

        // 4. Try transfer.sh
        if (!uploadSuccess) {
            println("🔄 Attempting upload to transfer.sh...")
            try {
                val url = URL("https://transfer.sh/${targetFile.name}")
                val connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.setRequestProperty("Content-Length", targetFile.length().toString())
                connection.connectTimeout = 45000
                connection.readTimeout = 45000
                
                targetFile.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val responseCode = connection.responseCode
                if (responseCode == 200 || responseCode == 201) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                    if (responseText.startsWith("http")) {
                        println("\n========================================")
                        println("🔗 EXTERNAL DOWNLOAD LINK (transfer.sh):")
                        println(responseText)
                        println("========================================\n")
                        uploadSuccess = true
                    }
                } else {
                    println("⚠️ Upload to transfer.sh failed with response code $responseCode")
                }
            } catch (e: Exception) {
                println("⚠️ Error uploading to transfer.sh: ${e.message}")
            }
        }

        // 5. Try free.keep.sh (anonymous temporary storage)
        if (!uploadSuccess) {
            println("🔄 Attempting upload to free.keep.sh...")
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
                    if (responseText.startsWith("http")) {
                        println("\n========================================")
                        println("🔗 EXTERNAL DOWNLOAD LINK (Keep.sh):")
                        println(responseText)
                        println("========================================\n")
                        uploadSuccess = true
                    }
                } else {
                    println("⚠️ Upload to keep.sh failed with response code $responseCode")
                }
            } catch (e: Exception) {
                println("⚠️ Error uploading to keep.sh: ${e.message}")
            }
        }

        // 6. Try file.io (multipart, temporary storage - Backup fall-back)
        if (!uploadSuccess) {
            println("🔄 Attempting upload to file.io (temporary storage)...")
            try {
                val res = uploadMultipart(
                    "https://file.io/",
                    "file",
                    targetFile,
                    mapOf("expires" to "1w")
                )
                if (res != null) {
                    val linkRegex = """"(link|url)"\s*:\s*"([^"]+)"""".toRegex()
                    val match = linkRegex.find(res)
                    val downloadLink = match?.groupValues?.get(2) ?: res
                    if (downloadLink.startsWith("http")) {
                        println("\n========================================")
                        println("🔗 EXTERNAL DOWNLOAD LINK (file.io):")
                        println(downloadLink)
                        println("========================================\n")
                        uploadSuccess = true
                    }
                }
            } catch (e: Exception) {
                println("⚠️ Error uploading to file.io: ${e.message}")
            }
        }
        
        if (!uploadSuccess) {
            println("\n❌ All direct cloud uploads failed. Please check your internet connectivity or build steps.\n")
        }
    }
}

