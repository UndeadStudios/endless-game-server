plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven (url = "https://m2.dv8tion.net/releases")
    flatDir {
        dirs("lib")
    }
}

dependencies {
    //implementation(":javacord-3.0.0-shaded")

    implementation("org.javacord:javacord:3.7.0")
    implementation(":RS2-Cache-Library")
    implementation(":toml4j-0.7.3-SNAPSHOT")
    implementation(":api-model-1.0")

    implementation("pl.allegro.finance:tradukisto:1.12.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.guava", "guava", "31.1-jre")
    implementation("org.projectlombok:lombok:1.18.22")
    val log4j = "2.19.0"
    implementation("org.apache.logging.log4j", "log4j-core", log4j)
    implementation("org.apache.logging.log4j", "log4j-1.2-api", log4j)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4j)
    implementation("com.jolbox", "bonecp", "0.8.0.RELEASE")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("mysql", "mysql-connector-java", "8.0.31")
    implementation("com.google.code.gson", "gson", "2.10")
    implementation("com.warrenstrange:googleauth:1.5.0")
    implementation("org.javacord:javacord:3.7.0")
    val sdcf4j = "v1.0.10"
    implementation("de.btobastian.sdcf4j:sdcf4j-core:$sdcf4j")
    implementation("de.btobastian.sdcf4j:sdcf4j-javacord:$sdcf4j")
    implementation("it.unimi.dsi", "fastutil", "8.5.11")
    implementation("io.github.classgraph", "classgraph", "4.8.152")
    implementation("commons-io", "commons-io", "2.11.0")
    implementation("org.ow2.asm", "asm-all", "5.2")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.apache.commons", "commons-text", "1.10.0")
    implementation("org.apache.commons", "commons-compress", "1.21")
    implementation("com.amazonaws", "aws-java-sdk", "1.12.366")
    implementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", "2.13.4")
    implementation("net.lingala.zip4j", "zip4j", "2.11.2")
    implementation("com.esotericsoftware", "kryo", "5.3.0")
    implementation("org.apache.commons", "commons-pool2", "2.11.1")
    implementation("org.jctools:jctools-core:4.0.1")
    implementation("net.openhft:chronicle-core:2.24ea3")
    implementation("net.openhft:affinity:3.23.2")
    // discord bot
    //implementation ("net.dv8tion:JDA:4.2.1_253")
    implementation ("net.dv8tion:JDA:5.0.0-alpha.4")

    /* Netty */
    val nettyVer = "4.1.86.Final"
    val nettyIoUringVer = "0.0.16.Final"

    implementation("io.netty", "netty-all", nettyVer)
    runtimeOnly("io.netty:netty-transport-native-epoll:$nettyVer:linux-aarch_64")
    runtimeOnly("io.netty:netty-transport-native-epoll:$nettyVer:linux-x86_64")
    runtimeOnly("io.netty:netty-transport-native-kqueue:$nettyVer:osx-x86_64")

    implementation("io.netty.incubator:netty-incubator-transport-native-io_uring:$nettyIoUringVer")
    runtimeOnly("io.netty.incubator:netty-incubator-transport-native-io_uring:$nettyIoUringVer:linux-aarch_64")
    runtimeOnly("io.netty.incubator:netty-incubator-transport-native-io_uring:$nettyIoUringVer:linux-x86_64")

    implementation("org.jetbrains:annotations:23.1.0")
}

kotlin {
    jvmToolchain(19)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

fun execTask(
    name: String, mainClassName: String = "com.zenyte.GameEngine", configure: (JavaExecSpec.() -> Unit)? = null
) = tasks.register(name, JavaExec::class.java) {
    group = ApplicationPlugin.APPLICATION_GROUP

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(mainClassName)
    jvmArgs(
        "-XX:+UseZGC",
        "-XX:-OmitStackTraceInFastThrow",
        "--add-opens=java.base/java.time=ALL-UNNAMED",
        "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"
    )

    enableAssertions = true
    if (hasProperty("args")) {
        val argsProperty = property("args")
        val argsList = argsProperty as List<*>
        if (argsList.isNotEmpty()) {
            args(argsList)
        }
    }

    configure?.invoke(this)
}

execTask("runMain") {
    args = listOf("main")
}

execTask("runOfflineDev") {
    args = listOf("offline_dev")
}

execTask("typeParser", "mgi.tools.parser.TypeParser") {
    args = listOf("--unzip")
}

execTask("mapPacker", "com.zenyte.game.util.MapPacker")

execTask("cachePacker", "com.zenyte.openrs.cache.CachePacking")
