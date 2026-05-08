plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.goldjucx.showcase"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.goldjucx.ui.showcase"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// ─── 自动从原项目同步 demo 代码和资源 ───
val sourceProject = file("/Users/gaojiaxiang/Downloads/手机管家_Demo")

tasks.register("syncFromSource") {
    val srcKt = sourceProject.resolve("app/src/main/java/com/miui/peepguard/ui/screens/UIShowcaseScreen.kt")
    val dstKt = file("src/main/java/com/goldjucx/showcase/ui/screens/UIShowcaseScreen.kt")
    val srcStrings = sourceProject.resolve("app/src/main/res/values/strings_ui_showcase.xml")
    val dstStrings = file("src/main/res/values/strings_ui_showcase.xml")
    val srcDrawable = sourceProject.resolve("app/src/main/res/drawable")
    val dstDrawable = file("src/main/res/drawable")

    doLast {
        // 同步 UIShowcaseScreen.kt（替换包名）
        if (srcKt.exists()) {
            dstKt.parentFile.mkdirs()
            dstKt.writeText(
                srcKt.readText()
                    .replace("package com.miui.peepguard.ui.screens", "package com.goldjucx.showcase.ui.screens")
                    .replace("import com.miui.peepguard.R", "import com.goldjucx.showcase.R")
                    .replace("import com.miui.peepguard.ui.theme.OnSurfaceQuaternary", "import com.goldjucx.showcase.ui.theme.OnSurfaceQuaternary")
            )
        }
        // 同步 strings
        if (srcStrings.exists()) {
            srcStrings.copyTo(dstStrings, overwrite = true)
        }
        // 同步 drawable（只复制 ic_* 和 illustration_*）
        srcDrawable.listFiles()?.filter {
            it.name.startsWith("ic_") || it.name.startsWith("illustration_") || it.name.startsWith("bg_")
        }?.forEach { it.copyTo(dstDrawable.resolve(it.name), overwrite = true) }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("syncFromSource")
}

dependencies {
    implementation(project(":goldjucx_ui"))
    implementation(platform("androidx.compose:compose-bom:2025.01.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
