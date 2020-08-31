import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
}
group = "nl.knaw.huygens.alexandria.alexandria"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("http://maven.huygens.knaw.nl/repository/")
}

dependencies {
    implementation("nl.knaw.huygens.alexandria:tagml:3.0.560-SNAPSHOT")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }
}
