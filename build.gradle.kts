plugins {
    //kotlin("multiplatform") version "1.6.10" apply false
    kotlin("jvm") version "1.6.10"
    //kotlin("js") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}


group = "me.alex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    //implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
    implementation("com.github.nwillc.ksvg:ksvg:master-SNAPSHOT")
}
//
//tasks.test {
//    useJUnit()
//}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}