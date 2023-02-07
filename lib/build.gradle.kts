plugins {
    kotlin("jvm") version "1.8.10"
    `maven-publish`
}
group = "com.github.andreytemn"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.vishna:watchservice-ktx:master-SNAPSHOT")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            group = groupId
            artifactId = rootProject.name
            version = project.version.toString()
        }
    }
}

kotlin {
    jvmToolchain(11)
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        )
    }
}