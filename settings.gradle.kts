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

// Every directory under services/ or libs/ with a build file becomes a subproject, so adding
// a service or library never requires editing this file (see templates/spring-service).
for (group in listOf("libs", "services")) {
    file(group).listFiles()
        ?.filter { File(it, "build.gradle.kts").exists() }
        ?.sorted()
        ?.forEach { include(":$group:${it.name}") }
}
