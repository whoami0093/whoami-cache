import org.jreleaser.model.Active

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jreleaser)
    id("maven-publish")
}

group = "io.github.whoami0093"
version = "1.0.1"
description = "A simple cache library"

repositories {
    mavenCentral()
}

dependencies {
    // slf4j
    implementation(libs.slf4j.api)
}

// Генерация sources.jar
tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

// Генерация javadoc.jar
tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            groupId = "io.github.whoami0093"
            artifactId = "whoami-cache"

            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))

            pom {
                name.set(project.properties["POM_NAME"].toString())
                description.set(project.description)
                url.set("https://github.com/whoami0093/whoami-cache")
                issueManagement {
                    url.set("https://github.com/whoami0093/whoami-cache/issues")
                }

                scm {
                    url.set("https://github.com/whoami0093/whoami-cache")
                    connection.set("scm:git://github.com/whoami0093/whoami-cache.git")
                    developerConnection.set("scm:git://github.com/whoami0093/whoami-cache.git")
                }

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("whoami0093")
                        name.set("Anton Tamarov")
                        email.set("anton.tamarov0093@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        inceptionYear = "2024"
        author("@whoami0093")
    }
    release {
        github {
            skipRelease = true
            skipTag = true
            sign = true
            branch = "main"
            branchPush = "main"
            overwrite = true
        }
    }
    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                setAuthorization("Basic")
                retryDelay = 60
            }
        }
    }
}