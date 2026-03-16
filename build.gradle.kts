plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    kotlin("plugin.jpa") version "1.9.25"
    id("jacoco")
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
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
    implementation("com.stripe:stripe-java:24.9.0")
    implementation(libs.jjwt.api)
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    
    // Email Notifications (Phase 8)
    implementation("com.sendgrid:sendgrid-java:4.10.2")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.1.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.1.0")
    
    // Security hardening (Phase 11)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Performance & Production (Phase 12)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("io.sentry:sentry-spring-boot-starter:6.28.0")
    
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
    mainClass.set("com.hotresvib.tools.FlywayRunnerKt")
}

tasks.register<JavaExec>("runFlywayDev") {
    group = "verification"
    description = "Run FlywayRunnerDev to apply migrations+seed to in-memory H2 (dev)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.hotresvib.tools.FlywayRunnerDevKt")
}

tasks.register<JavaExec>("generateBcrypt") {
    group = "utility"
    description = "Generate bcrypt hashes for demo passwords"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.hotresvib.tools.HashGeneratorKt")
}

// Configure bootRun to honor a project property `-Pprofile=...` or environment variable
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    val profileFromProp = if (project.hasProperty("profile")) project.property("profile").toString() else null
    val profile = profileFromProp ?: System.getenv("SPRING_PROFILES_ACTIVE") ?: "dev"
    systemProperty("spring.profiles.active", profile)
}

// Convenience task to run with dev profile (avoids argument parsing issues on Windows shells)
tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("bootRunDev") {
    group = "application"
    description = "Run the application with the dev Spring profile"
    systemProperty("spring.profiles.active", "dev")
    // Include developmentOnly dependencies (devtools) on the bootRun classpath so restart works
    classpath = sourceSets["main"].runtimeClasspath + configurations.getByName("developmentOnly")
    mainClass.set("com.hotresvib.HotResvibApplicationKt")
}

// JaCoCo test coverage configuration
jacoco {
    toolVersion = "0.8.10"
}

// Ensure test report is generated after running tests
tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

// Configure JaCoCo report
tasks.named<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val classesDirs = fileTree("build/classes/kotlin/main") { include("**/*.class") }
    classDirectories.setFrom(files(classesDirs))
    sourceDirectories.setFrom(files("src/main/kotlin"))
    executionData.setFrom(files("build/jacoco/test.exec"))
}

// Enforce minimum coverage
tasks.named<org.gradle.testing.jacoco.tasks.JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.register("checkCoverage") {
    dependsOn(tasks.named("jacocoTestReport"), tasks.named("jacocoTestCoverageVerification"))
}

