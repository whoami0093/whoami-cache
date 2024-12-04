plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.whoami"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // slf4j
    implementation("org.slf4j:slf4j-api:2.0.16")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}