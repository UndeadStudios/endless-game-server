allprojects {
    apply(plugin = "java")
    group = "com.zenyte"
    version = "1.0"
}

subprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        "compile"(kotlin("stdlib-jdk8"))
        "compile"(kotlin("reflect:1.3.61"))
        "compile"("io.lettuce:lettuce-core:5.2.1.RELEASE")
        "compile"("io.github.microutils:kotlin-logging:1.7.8")
        "compile"("ch.qos.logback:logback-classic:1.2.3")
        "compile"("com.google.code.gson:gson:2.8.6")
        "compile"("com.squareup.okhttp3:okhttp:4.2.2")
    }
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
    
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
    }
}

project(":discord") {
    dependencies {
        "implementation"(project(":common"))
    }
}

project(":api") {
    dependencies {
        "implementation"(project(":common"))
    }
}
