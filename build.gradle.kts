plugins {
    kotlin("multiplatform") version "1.8.10"
    id("org.jetbrains.dokka") version "1.8.10"
    id("maven-publish")
}

group = "org.llesha.regina"
version = "0.9.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        binaries.executable()
        browser {
        }
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}