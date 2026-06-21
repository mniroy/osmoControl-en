import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val libs = target.extensions.getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java).named("libs")
        val applicationExtension = target.extensions.findByType(ApplicationExtension::class.java)
        val libraryExtension = target.extensions.findByType(LibraryExtension::class.java)

        // Apply the Kotlin Compose plugin (bundled in Kotlin 2.0+)
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        when {
            applicationExtension != null -> {
                applicationExtension.buildFeatures.compose = true
            }
            libraryExtension != null -> {
                libraryExtension.buildFeatures.compose = true
            }
            else -> error("Compose convention requires an Android module.")
        }

        target.dependencies {
            add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-foundation").get())
            add("implementation", libs.findLibrary("androidx-compose-material3").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
        }
    }
}

