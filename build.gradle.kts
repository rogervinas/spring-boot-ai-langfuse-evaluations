import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.kotlin.dsl.implementation
import java.util.Properties

plugins {
    val kotlinVersion = "2.3.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.rogervinas"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

val springAiVersion = "2.0.0-M4"
val otelInstrumentationVersion = "2.26.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-webclient")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")

    // TODO remove when spring-ai is updated to use Jackson 3
    implementation("org.springframework.boot:spring-boot-jackson2")

    // ollama
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")

    // gemini
    implementation("org.springframework.ai:spring-ai-starter-model-google-genai")
    implementation("org.springframework.ai:spring-ai-starter-model-google-genai-embedding")

    // bedrock
    implementation("org.springframework.ai:spring-ai-starter-model-bedrock")
    implementation("org.springframework.ai:spring-ai-starter-model-bedrock-converse")

    // vector store
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector")
    runtimeOnly("org.postgresql:postgresql")

    // open telemetry
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-webflux-test")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:$otelInstrumentationVersion")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events(PASSED, SKIPPED, FAILED)
    }
    setSystemProperties { systemProperty(it.first, it.second) }
    val springProfilesActive = System.getenv("SPRING_PROFILES_ACTIVE") ?: "gemini"
    systemProperty("spring.profiles.active", "test,$springProfilesActive")
}

tasks.named<JavaExec>("bootRun") {
    setSystemProperties { systemProperty(it.first, it.second) }
    require(System.getenv("SPRING_PROFILES_ACTIVE") != null) {
        "SPRING_PROFILES_ACTIVE must be set (e.g. ollama, gemini, bedrock)"
    }
}

private fun setSystemProperties(setSystemProperty: (Pair<String, Any>) -> Unit) {
    val systemPropertiesFile = project.rootProject.file("system.properties")
    if (systemPropertiesFile.exists()) {
        systemPropertiesFile.inputStream().use { inputStream ->
            Properties().apply {
                load(inputStream)
            }.forEach {
                setSystemProperty(it.key.toString() to it.value)
            }
        }
    }
}
