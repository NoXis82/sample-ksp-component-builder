import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}
//tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
//    kotlinOptions { jvmTarget = "11" }
//}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.0-1.0.13")
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}