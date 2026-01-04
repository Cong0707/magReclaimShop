import io.izzel.taboolib.gradle.*

plugins {
    kotlin("jvm") version "2.3.0-RC2"
    id("xyz.jpenilla.run-paper") version "2.3.1"

    id("io.izzel.taboolib") version "2.0.27"
}

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    // 改成 Paper API
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.7")

    compileOnly("ink.ptms.core:v12001:12001:mapped")
    compileOnly("ink.ptms.core:v12001:12001:universal")

    compileOnly("me.clip:placeholderapi:2.11.7")

    taboo("org.jetbrains.exposed:exposed-core:0.41.1")
    taboo("org.jetbrains.exposed:exposed-dao:0.41.1")
    taboo("org.jetbrains.exposed:exposed-java-time:0.41.1")
    taboo("org.jetbrains.exposed:exposed-jdbc:0.41.1")

    taboo("com.h2database:h2:2.2.224")
    taboo("com.mysql:mysql-connector-j:8.0.33")
}

taboolib {
    description {
        // 开发者
        contributors {
            name("cong0707")
        }
        dependencies {
            //name("PlaceholderAPI")
        }
    }
    env {
        install(Basic, Bukkit)
        install(BukkitNMS, BukkitNMSUtil, BukkitFakeOp)
        install(MinecraftChat, I18n)
        install(Kether)
        install(BukkitHook)
        install(BukkitNavigation, BukkitUI, BukkitUtil)
    }
    version { taboolib = "6.2.4-abd325ee" }
}

tasks {
    runServer {
        // 指定 Paper 版本
        minecraftVersion("1.20.1")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}
