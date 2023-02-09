import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    kotlin("plugin.jpa") version "1.8.0"
}

group = "com.lovemap"
version = "1.17.4"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-core:1.10.3")

    implementation("com.google.firebase:firebase-admin:9.1.1")

//    Did not work out: com.google.api.gax.rpc.UnavailableException: io.grpc.StatusRuntimeException: UNAVAILABLE: Channel shutdown invoked
//    implementation("com.google.cloud:spring-cloud-gcp-starter:3.4.2")
//    implementation("io.micrometer:micrometer-registry-stackdriver")
//    implementation("com.google.cloud:google-cloud-monitoring:3.10.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("io.github.microutils:kotlin-logging:3.0.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.0")
    implementation("com.javadocmd:simplelatlng:1.4.0")
    implementation("com.google.maps:google-maps-services:2.1.2")
    implementation("com.google.cloud:google-cloud-storage:2.16.0")
    implementation("com.mailjet:mailjet-client:5.2.1")

    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-api:2.0.2")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")

    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
