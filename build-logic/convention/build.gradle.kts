plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.8.0")
    compileOnly(kotlin("gradle-plugin", version = "2.1.20"))
}

gradlePlugin {
    plugins {
        register("androidApplicationConvention") {
            id = "osmo.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryConvention") {
            id = "osmo.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("jvmLibraryConvention") {
            id = "osmo.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("composeConvention") {
            id = "osmo.android.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}
