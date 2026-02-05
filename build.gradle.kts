plugins {
    kotlin("jvm") version "2.3.0"
    `maven-publish`
    id("hytale-mod") version "0.+"
}

val javaVersion = 25
group = "me.clcondorcet"
version = "v0.1.0"

val server_version = "*"
val plugin_description = "Core plugin of BoiteEnCarton Games"
val plugin_author = "Albador"
val plugin_website = ""
val plugin_main_entrypoint = "me.clcondorcet.boiteaoutils.BoiteAOutils"

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)
}

hytale {
    // uncomment if you want to add the Assets.zip file to your external libraries;
    // ⚠️ CAUTION, this file is very big and might make your IDE unresponsive for some time!
    //
    // addAssetsDependency = true

    // uncomment if you want to develop your mod against the pre-release version of the game.
    //
    // updateChannel = "pre-release"
}

kotlin {
    jvmToolchain(javaVersion)
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    withSourcesJar()
}

tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to project.group,
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to server_version,

        "plugin_description" to plugin_description,
        "plugin_website" to plugin_website,

        "plugin_main_entrypoint" to plugin_main_entrypoint,
        "plugin_author" to plugin_author
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a jar archive containing the main classes and all dependencies."
    archiveClassifier.set("all")

    // Include your own code
    from(sourceSets.main.get().output)

    // Include all dependencies (Kotlin stdlib, etc.)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.contains("kotlin-stdlib") }
            .map { zipTree(it) }
    }) {
        // Prevent duplicate META-INF files from crashing the build
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    manifest {
        attributes["Main-Class"] = plugin_main_entrypoint
        attributes["Implementation-Version"] = project.version
    }
}

publishing {
    repositories {
        // This is where you put repositories that you want to publish to.
        // Do NOT put repositories for your dependencies here.
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

val syncAssets = tasks.register<Copy>("syncAssets") {
    group = "hytale"
    description = "Automatically syncs assets from Build back to Source after server stops."

    // Take from the temporary build folder (Where the game saved changes)
    from(layout.buildDirectory.dir("resources/main"))

    // Copy into your actual project source (Where your code lives)
    into("src/main/resources")

    // IMPORTANT: Protect the manifest template from being overwritten
    exclude("manifest.json")
    includeEmptyDirs = false

    // If a file exists, overwrite it with the new version from the game
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    doLast {
        println("✅ Assets successfully synced from Game to Source Code!")
    }
}

afterEvaluate {
    // Now Gradle will find it, because the plugin has finished working
    val targetTask = tasks.findByName("runServer") ?: tasks.findByName("server")

    if (targetTask != null) {
        targetTask.finalizedBy(syncAssets)
        logger.lifecycle("✅ specific task '${targetTask.name}' hooked for auto-sync.")
    } else {
        logger.warn("⚠️ Could not find 'runServer' or 'server' task to hook auto-sync into.")
    }
}
