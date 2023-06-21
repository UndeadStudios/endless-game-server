import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

dependencies {
    "compile"("com.squareup.okhttp3:okhttp:4.2.2")
    
    "compile"("org.apache.commons:commons-text:1.8")
    "compile"("org.jsoup:jsoup:1.13.1")
}

tasks.register<ShadowJar>("modelJar") {
    minimize()
    archiveBaseName.set("api-model")
    from(sourceSets.main.get().output) {
        include("com/zenyte/api/model/**")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}