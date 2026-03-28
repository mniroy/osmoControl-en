plugins {
    id("osmo.jvm.library")
}

dependencies {
    implementation(project(":core-protocol"))
    implementation(project(":core-ble"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
}
