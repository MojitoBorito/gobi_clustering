plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("info.picocli:picocli:4.7.7")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveFileName.set("readsimulator.jar")
    manifest {
        attributes["Main-Class"] = "ReadSimulation.Runner"
    }
}