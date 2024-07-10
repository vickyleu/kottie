import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.dokka)
    id("maven-publish")
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
compose.resources {
    publicResClass = false
    packageOfResClass = "org.airbnb.fork.kottie"
    generateResClass = never
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



buildscript {
    dependencies {
        val dokkaVersion = libs.versions.dokka.get()
        classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    }
}

//group = "io.github.ltttttttttttt"
////上传到mavenCentral命令: ./gradlew publishAllPublicationsToSonatypeRepository
////mavenCentral后台: https://s01.oss.sonatype.org/#stagingRepositories
//version = "${libs.versions.compose.plugin.get()}.beta1"

group = "com.vickyleu.kottie"
version = "1.0.2"


tasks.withType<PublishToMavenRepository> {
    val isMac = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
    archiveClassifier = "javadoc"
}


tasks.dokkaHtml {
    // outputDirectory = layout.buildDirectory.get().resolve("dokka")
    offlineMode = false
    moduleName = "kottie"

    // See the buildscript block above and also
    // https://github.com/Kotlin/dokka/issues/2406
//    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
////        customAssets = listOf(file("../asset/logo-icon.svg"))
////        customStyleSheets = listOf(file("../asset/logo-styles.css"))
//        separateInheritedMembers = true
//    }

    dokkaSourceSets {
        configureEach {
            reportUndocumented = true
            noAndroidSdkLink = false
            noStdlibLink = false
            noJdkLink = false
            jdkVersion = 17
            // sourceLink {
            //     // Unix based directory relative path to the root of the project (where you execute gradle respectively).
            //     // localDirectory.set(file("src/main/kotlin"))
            //     // URL showing where the source code can be accessed through the web browser
            //     // remoteUrl = uri("https://github.com/mahozad/${project.name}/blob/main/${project.name}/src/main/kotlin").toURL()
            //     // Suffix which is used to append the line number to the URL. Use #L for GitHub
            //     remoteLineSuffix = "#L"
            // }
        }
    }
}

val properties = Properties().apply {
    runCatching { rootProject.file("local.properties") }
        .getOrNull()
        .takeIf { it?.exists() ?: false }
        ?.reader()
        ?.use(::load)
}
// For information about signing.* properties,
// see comments on signing { ... } block below
val environment: Map<String, String?> = System.getenv()
extra["githubToken"] = properties["github.token"] as? String
    ?: environment["GITHUB_TOKEN"] ?: ""

publishing {
    val projectName = rootProject.name
    repositories {
        /*maven {
            name = "CustomLocal"
            url = uri("file://${layout.buildDirectory.get()}/local-repository")
        }
        maven {
            name = "MavenCentral"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extra["ossrhUsername"]?.toString()
                password = extra["ossrhPassword"]?.toString()
            }
        }*/
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/vickyleu/${projectName}")
            credentials {
                username = "vickyleu"
                password = extra["githubToken"]?.toString()
            }
        }
    }

    afterEvaluate {
        publications.withType<MavenPublication> {
            artifactId = artifactId.replace(project.name, projectName.lowercase())
            artifact(javadocJar) // Required a workaround. See below
            pom {
                url = "https://github.com/vickyleu/${projectName}"
                name = projectName
                description = """
                Visit the project on GitHub to learn more.
            """.trimIndent()
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "Apache-2.0 License"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "ismai117"
                        name = "ismai117"
                        email = ""
                        roles = listOf("Mobile Developer")
                        timezone = "GMT+8"
                    }
                }
                contributors {
                    // contributor {}
                }
                scm {
                    tag = "HEAD"
                    url = "https://github.com/vickyleu/${projectName}"
                    connection = "scm:git:github.com/vickyleu/${projectName}.git"
                    developerConnection = "scm:git:ssh://github.com/vickyleu/${projectName}.git"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/vickyleu/${projectName}/issues"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/vickyleu/${projectName}/actions"
                }
            }
        }
    }
}

// TODO: Remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//  Thanks to KSoup repository for this code snippet
tasks.withType(AbstractPublishToMaven::class).configureEach {
    dependsOn(tasks.withType(Sign::class))
}

// * Uses signing.* properties defined in gradle.properties in ~/.gradle/ or project root
// * Can also pass from command line like below
// * ./gradlew task -Psigning.secretKeyRingFile=... -Psigning.password=... -Psigning.keyId=...
// * See https://docs.gradle.org/current/userguide/signing_plugin.html
// * and https://stackoverflow.com/a/67115705
/*signing {
    sign(publishing.publications)
}*/

//
//mavenPublishing {
////    publishToMavenCentral(SonatypeHost.DEFAULT)
//    // or when publishing to https://s01.oss.sonatype.org
//    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
//    signAllPublications()
//    coordinates("io.github.ismai117", "kottie", "1.7.3")
//
//    pom {
//        name.set(project.name)
//        description.set("Kotlin Multiplatform Animation Library")
//        inceptionYear.set("2024")
//        url.set("https://github.com/ismai117/kottie/")
//        licenses {
//            license {
//                name.set("The Apache License, Version 2.0")
//                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//            }
//        }
//        developers {
//            developer {
//                id.set("ismai117")
//                name.set("ismai117")
//                url.set("https://github.com/ismai117/")
//            }
//        }
//        scm {
//            url.set("https://github.com/ismai117/kottie/")
//            connection.set("scm:git:git://github.com/ismai117/kottie.git")
//            developerConnection.set("scm:git:ssh://git@github.com/ismai117/kottie.git")
//        }
//    }
//}

