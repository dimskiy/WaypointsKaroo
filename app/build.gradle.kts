import com.google.gson.JsonParser
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.androidx.room)

    alias(libs.plugins.google.services)
    alias(libs.plugins.google.crashlytics)
}

val generateKarooManifest by tasks.registering {
    val inputFile = file("$rootDir/manifest-karoo-template.json")
    val outputDir = layout.buildDirectory.dir("generated/assets")
    val outputFile = outputDir.map { it.file("manifest-karoo.json") }

    inputs.file(inputFile)
    outputs.file(outputFile)

    doLast {
        val versionCode = project.properties["APP_VERSION_CODE"].toString().toInt()
        val versionName = project.properties["APP_VERSION_NAME"].toString()
        val packageName = project.properties["APPLICATION_ID"].toString()

        val jsonContent = inputFile.readText()
        val jsonElement = JsonParser.parseString(jsonContent)
        val jsonObject = jsonElement.asJsonObject

        jsonObject.addProperty("latestVersionCode", versionCode)
        jsonObject.addProperty("latestVersion", versionName)
        jsonObject.addProperty("packageName", packageName)

        outputFile.get().asFile.parentFile.mkdirs()
        outputFile.get().asFile.writeText(jsonObject.toString())

        println("Karoo manifest generated: ${outputFile.get()}")
    }
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = project.properties["APPLICATION_ID"].toString()
    compileSdk = 35

    defaultConfig {
        applicationId = project.properties["APPLICATION_ID"].toString()
        minSdk = 23
        targetSdk = 35
        versionCode = project.properties["APP_VERSION_CODE"].toString().toInt()
        versionName = project.properties["APP_VERSION_NAME"].toString()
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("profiling") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            isJniDebuggable = true
            isPseudoLocalesEnabled = true
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }

    tasks.named("assemble") {
        dependsOn(generateKarooManifest)
    }
}

dependencies {
    implementation(libs.hammerhead.karoo.ext)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.androidx.lifeycle)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.datastore)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.android)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.bundles.androidx.room)
    ksp(libs.androidx.room.compiler)

    implementation(libs.google.gson)
    implementation(libs.swipeview.compose)
    implementation(libs.timber)

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.analytics)
    implementation(libs.google.firebase.crashlytics)
}
