plugins {
    id("java")
}

group = "net.leonemc"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // -- Loaded by the plugin loader! --
    compileOnly("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("org.mongodb:mongodb-driver-sync:5.0.0")
    compileOnly("ch.jalu:configme:1.4.1")
    // ------------------------
}

tasks {
    java {
        // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)

        options.compilerArgs.plus("-parameters")
        options.isFork = true
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}