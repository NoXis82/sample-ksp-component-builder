
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.noxis.contact"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("debug").java.srcDir("build/generated/ksp/debug/kotlin")
        getByName("release").java.srcDir("build/generated/ksp/release/kotlin")
    }
}

ksp {
    arg("test_parameter_1", "false")
    arg("test_parameter_2", "100")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.okhttp)

    implementation(libs.dagger.compiler)
    ksp(libs.dagger.compiler)

    implementation(project(":core"))
    implementation(project(":network"))
    ksp(project(":ksp-component-builder"))
}