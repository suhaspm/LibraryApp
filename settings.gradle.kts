rootProject.name = "mediahub"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

// Every directory under services/ with a build file becomes a subproject, so adding a
// service never requires editing this file (see templates/spring-service).
file("services").listFiles()
    ?.filter { File(it, "build.gradle.kts").exists() }
    ?.sorted()
    ?.forEach { include(":services:${it.name}") }
