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
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
    implementation("com.stripe:stripe-java:24.9.0")
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
