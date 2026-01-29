plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.hotresvib"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("com.h2database:h2")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
    implementation(libs.jjwt.api)
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    implementation(libs.kotlin.reflect)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("com.hotresvib.HotResvibApplicationKt")
}

tasks.withType<org.gradle.api.tasks.compile.JavaCompile> {
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.register<JavaExec>("inspectDb") {
    group = "verification"
    description = "Run DbInspector to show Flyway schema history"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.hotresvib.tools.DbInspector")
}

tasks.register<JavaExec>("runFlyway") {
    group = "verification"
    description = "Run FlywayRunner to apply migrations to file DB and print history"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.hotresvib.tools.FlywayRunner")
}
