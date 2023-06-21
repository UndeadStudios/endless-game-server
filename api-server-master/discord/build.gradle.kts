import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.61"
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "com.zenyte.discord.MainKt"
}

dependencies {
    compile("io.github.classgraph:classgraph:4.8.58")
    compile("net.dv8tion:JDA:4.1.1_146")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<ShadowJar> {
    baseName = "discord"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
