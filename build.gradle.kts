plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
}
allprojects {
    if (tasks.findByName("testClasses") == null) {
        try {
            tasks.register("testClasses")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Version should be inherited from parent

    // Optionally configure plugin
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.0.1")
    }
}

tasks.register<Copy>("setUpGitHooks") {
    group = "help"
    from("$rootDir/.hooks")
    into("$rootDir/.git/hooks")
}
