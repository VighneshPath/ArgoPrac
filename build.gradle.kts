plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlin.kapt") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.minimal.application") version "3.7.10"
    id("com.palantir.docker") version "0.35.0"
}

version = "0.1.3"
group = "example.micronaut"

val kotlinVersion=project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}

application {
    mainClass.set("example.micronaut.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
//graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("example.micronaut.*")
    }
}


docker {
    name = project.name
    setDockerfile(File("Dockerfile"))
    copySpec.from("build/libs").into("libs")
    buildArgs(mapOf("version" to "${project.version}"))
    tag("Registry", "${System.getenv("REGISTRY_URL")}/${project.name}:${project.version}")
}


tasks.getByName("docker").onlyIf {
    !tasks.getByName("shadowJar").state.upToDate
}

tasks.all {
    if (this.name.startsWith("dockerPush")) {
        this.onlyIf {
            !tasks.getByName("docker").state.skipped
        }
    }
}

tasks.all {
    if (this.name.startsWith("dockerTag")) {
        this.onlyIf {
            !tasks.getByName("docker").state.skipped
        }
    }
}

docker.dependsOn(tasks.getByName("build"))
tasks.getByName("dockerPrepare").dependsOn(tasks.getByName("jar"))
tasks.getByName("dockerPrepare").dependsOn(tasks.getByName("shadowJar"))
