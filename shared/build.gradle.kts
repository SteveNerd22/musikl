import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    jvm()

    sourceSets {
        val jvmCommonMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.newpipe.extractor)
                implementation(libs.okhttp)
            }
        }

        androidMain {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation(libs.compose.uiToolingPreview)
            }
        }

        jvmMain {
            dependsOn(jvmCommonMain)
            dependencies {
                implementation(libs.vlcj)

                val os = System.getProperty("os.name").lowercase()
                val classifier = when {
                    os.contains("win") -> "win"
                    os.contains("mac") -> "mac"
                    os.contains("nux") -> "linux"
                    else -> "win"
                }

                implementation("${libs.javafx.base.get()}:$classifier")
                implementation("${libs.javafx.media.get()}:$classifier")
                implementation("${libs.javafx.graphics.get()}:$classifier")
            }
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.rgbcolor.musikl.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}