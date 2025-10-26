plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.joaorihan"
version = "1.4.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    
    implementation("org.apache.commons:commons-text:1.12.0")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
}
