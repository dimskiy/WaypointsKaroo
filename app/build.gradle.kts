import com.google.gson.JsonParser

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
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "no_file")
            storePassword = System.getenv("KEYSTORE_PASSWORD").orEmpty()
            keyAlias = System.getenv("KEY_ALIAS").orEmpty()
            keyPassword = System.getenv("KEY_PASSWORD").orEmpty()
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
}

afterEvaluate {
    val generateKarooAssetsManifest by tasks.registering {
        val readmeFile = file("$rootDir/README.md")
        if (!readmeFile.exists()) {
            error("'${readmeFile.name}' not found - cannot generate Karoo manifest")
        }

        val manifestTemplateFile = file("$rootDir/manifest-karoo-template.json")
        if (!manifestTemplateFile.exists()) {
            error("'${manifestTemplateFile.name}' not found - cannot generate Karoo manifest")
        }

        val outputDir = layout.buildDirectory.dir("outputs/apk/release/assets")
        val outputFile = outputDir.map { it.file("manifest-karoo.json") }

        inputs.file(manifestTemplateFile)
        outputs.file(outputFile)

        val getSectionText: (String) -> String = { sectionName ->
            val readmeLines = readmeFile.readLines()
            val description = StringBuilder()

            var isDescriptionStarted = false
            for (line in readmeLines) {
                val lineTrimmed = line.trim()
                when {
                    lineTrimmed == sectionName -> isDescriptionStarted = true
                    isDescriptionStarted && lineTrimmed.contains("## ") -> break
                    isDescriptionStarted -> description.appendLine(line)
                    else -> {}
                }
            }
            description.toString()
        }

        doLast {
            val versionCode = project.properties["APP_VERSION_CODE"].toString().toInt()
            val versionName = project.properties["APP_VERSION_NAME"].toString()
            val packageName = project.properties["APPLICATION_ID"].toString()
            val description = getSectionText("## Description")
            val releaseNotes = getSectionText("## Release notes")

            val jsonContent = manifestTemplateFile.readText()
            val jsonElement = JsonParser.parseString(jsonContent)
            val jsonObject = jsonElement.asJsonObject

            jsonObject.addProperty("latestVersionCode", versionCode)
            jsonObject.addProperty("latestVersion", versionName)
            jsonObject.addProperty("packageName", packageName)
            jsonObject.addProperty("description", description)
            jsonObject.addProperty("releaseNotes", releaseNotes)

            outputFile.get().asFile.parentFile.mkdirs()
            outputFile.get().asFile.writeText(jsonObject.toString())

            println("Karoo manifest generated: ${outputFile.get()}")
        }
    }

    val copyAppIconToAssets by tasks.registering {
        doLast {
            val iconFile = file("src/main/res/mipmap-xxhdpi/ic_launcher.webp")
            val destinationDir =
                layout.buildDirectory.dir("outputs/apk/release/assets").get().asFile

            if (!iconFile.exists()) {
                error("No app icon found: '${iconFile.absolutePath}'")
            }

            destinationDir.mkdirs()
            val destinationFileName = "app_icon.${iconFile.extension}"
            iconFile.copyTo(File(destinationDir.path, destinationFileName), overwrite = true)

            println("App icon '${iconFile.name}' copied to '${destinationDir.path}' as '$destinationFileName'")
        }
    }

    tasks.named("assembleRelease") {
        finalizedBy(generateKarooAssetsManifest)
        finalizedBy(copyAppIconToAssets)
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
