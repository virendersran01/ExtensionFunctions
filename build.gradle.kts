// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.kotlin.parcelize) apply false
    //alias(libs.plugins.compose) apply false
    id ("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
apply("${project.rootDir}/buildscripts/toml-updater-config.gradle")