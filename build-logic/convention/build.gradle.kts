plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.7.3")
    compileOnly(kotlin("gradle-plugin", version = "1.9.25"))
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
