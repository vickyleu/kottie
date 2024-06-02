import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.konan.target.linker

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    iosArm64 {
        compilations.getByName("main") {
            val Lottie by cinterops.creating {
                defFile("src/nativeInterop/cinterop/Lottie.def")
                val path = "$rootDir/vendor/Lottie.xcframework/ios-arm64"
                compilerOpts("-F$path", "-framework", "Lottie", "-rpath", path)
                extraOpts += listOf("-compiler-option", "-fmodules")
            }
        }
    }
    listOf(
        iosX64(),
        iosSimulatorArm64()
    ).forEach {
        it.compilations.getByName("main") {
            val Lottie by cinterops.creating {
                defFile("src/nativeInterop/cinterop/Lottie.def")
                val path = "$rootDir/vendor/Lottie.xcframework/ios-arm64_x86_64-simulator"
                compilerOpts("-F$path", "-framework", "Lottie", "-rpath", path)
                extraOpts += listOf("-compiler-option", "-fmodules")
            }
        }
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(libs.ktor.client.core)
                implementation(compose.components.resources)
            }
        }

        val skiaMain = create("skiaMain") {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependencies {
                api(libs.androidx.activityCompose)
                api(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
                implementation(libs.androidLottie)
                implementation(libs.ktor.client.android)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(skiaMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val jvmMain by getting {
            dependsOn(skiaMain)
            dependencies {
                implementation(libs.ktor.client.java)
            }
        }

        val jsMain by getting {
            dependsOn(skiaMain)
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val wasmJsMain by getting {
            dependsOn(skiaMain)
        }

    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = "com.myapplication.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

mavenPublishing {
//    publishToMavenCentral(SonatypeHost.DEFAULT)
    // or when publishing to https://s01.oss.sonatype.org
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
    signAllPublications()
    coordinates("io.github.ismai117", "kottie", "1.7.3")

    pom {
        name.set(project.name)
        description.set("Kotlin Multiplatform Animation Library")
        inceptionYear.set("2024")
        url.set("https://github.com/ismai117/kottie/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ismai117")
                name.set("ismai117")
                url.set("https://github.com/ismai117/")
            }
        }
        scm {
            url.set("https://github.com/ismai117/kottie/")
            connection.set("scm:git:git://github.com/ismai117/kottie.git")
            developerConnection.set("scm:git:ssh://git@github.com/ismai117/kottie.git")
        }
    }
}

