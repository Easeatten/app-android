rootProject.name = "Easeatten"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

include(":app")
