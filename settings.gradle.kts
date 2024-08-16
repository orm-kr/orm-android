pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //kakao login
        maven { url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/") }
        //kakao map
        maven { url = java.net.URI("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/") }
        //graph
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "orm"
include(":app")
