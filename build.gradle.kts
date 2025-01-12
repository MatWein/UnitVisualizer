import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(properties("platformVersion"))
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "mw.unitv"
        name = properties("pluginName_")
        version = properties("pluginVersion")
        description =
            """
            Simple Intellij IDEA plugin to provide layered class icons for unit tested classes, similar to MoreUnit in Eclipse.<br/>
            It also provides functionality to automatically move test classes when moving tested classes.
            """.trimIndent()
        changeNotes =
            """
            [1.8.0 - 2025-01-12]<br/>
            Updated plugin for IDEA 2024.3
            """.trimIndent()

        ideaVersion {
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, properties("platformVersion"))
            ide(IntelliJPlatformType.IntellijIdeaUltimate, properties("platformVersion"))
            recommended()
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = properties("javaVersion")
        targetCompatibility = properties("javaVersion")
    }

    verifyPlugin {}
}
