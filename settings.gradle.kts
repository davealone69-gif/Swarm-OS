pluginManagement {
    repositories {
        // Local vendored Maven repo — must be populated via scripts/vendor-agp.sh before first offline build
        maven {
            name = "LocalVendored"
            url = uri("${rootDir}/libs/maven-repo")
        }
        // Fallback to remote repos when NOT running fully offline.
        // Remove or comment these out once the local repo is fully populated.
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Same local vendored repo for regular dependencies
        maven {
            name = "LocalVendored"
            url = uri("${rootDir}/libs/maven-repo")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "SwarmOS"
include(":app")
