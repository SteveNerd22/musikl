import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.material3)

    implementation(libs.compose.materialIconsCore)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
}

compose.desktop {
    application {
        mainClass = "io.rgbcolor.musikl.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "Musikl"
            packageVersion = if (System.getProperty("os.name").lowercase().contains("mac")) {
                "1.0.0"
            } else {
                "0.1.1"
            }

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                menu = true
                shortcut = true
            }
            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
                bundleID = "io.rgbcolor.musikl"
            }
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }

        buildTypes {
            release {
                proguard {
                    isEnabled.set(false)
                    configurationFiles.from(project.file("proguard-rules.pro"))
                }
            }
        }
    }
}