import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "nl.knaw.huygens.tag"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.4.0"
    `maven-publish`
}

repositories {
    mavenLocal()
    maven("http://maven.huygens.knaw.nl/repository/")
    mavenCentral()
}

dependencies {
    implementation("nl.knaw.huygens.alexandria:tagml:3.0.560-SNAPSHOT")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

publishing {
    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "$group"
            artifactId = "tag-mct"
            version = "$version"
            from(components["java"])
        }
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }
}