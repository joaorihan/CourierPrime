plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.joaorihan"
version = "1.4.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs & ModelEngine
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.84-stable")
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    
    // MythicMobs & ModelEngine (soft dependencies)
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.6")
    
    // https://mvnrepository.com/artifact/org.apache.commons/commons-text
    implementation("org.apache.commons:commons-text:1.10.0")
}

tasks {
    runServer {
        minecraftVersion("26.2")
    }

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
