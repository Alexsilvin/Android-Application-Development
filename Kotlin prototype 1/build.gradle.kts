import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.23"
    application
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.poi:poi-ooxml-schemas:4.1.2")
}

application {
    mainClass.set("MainKt")
}

val excelCalculatorArgs = providers.gradleProperty("appArgs")
    .orElse("")
    .map { argsLine ->
        argsLine.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

tasks.register<JavaExec>("runExcelGradeCalculator") {
    group = "application"
    description = "Runs the Excel-based student grade calculator."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ExcelStudentGradeCalculatorKt")
    standardInput = System.`in`
    doFirst {
        args(excelCalculatorArgs.get())
    }
}