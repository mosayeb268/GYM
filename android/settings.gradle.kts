pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KineticCareOS"

// ماژول‌های اپلیکیشن و هسته
include(":app")
include(":core:localization")
include(":core:designsystem")
include(":core:model")
include(":core:network")

// ماژول‌های ویژگی‌ها
include(":feature:auth")
include(":feature:turnstile")
include(":feature:assessment")
include(":feature:workout")
include(":feature:dashboard")
