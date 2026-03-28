plugins {
    id("osmo.jvm.library")
}

dependencies {
    implementation(project(":core-session"))
    implementation(libs.kotlinx.coroutines.core)
}
