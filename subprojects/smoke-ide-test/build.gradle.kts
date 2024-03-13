import gradlebuild.basics.gradleProperty
import gradlebuild.integrationtests.tasks.SmokeIdeTest
import gradlebuild.integrationtests.addDependenciesAndConfigurations

plugins {
    id("gradlebuild.internal.java")
}

description = "Tests are checking Gradle behavior during IDE synchronization process"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    maven {
        url = uri("https://www.jetbrains.com/intellij-repository/releases")
    }

    maven {
        url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
}

val smokeIdeTestSourceSet = sourceSets.create("smokeIdeTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

addDependenciesAndConfigurations("smokeIde")
val smokeIdeTestImplementation: Configuration by configurations
val smokeIdeTestDistributionRuntimeOnly: Configuration by configurations

plugins.withType<IdeaPlugin> {
    with(model) {
        module {
            testSources.from(smokeIdeTestSourceSet.java.srcDirs, smokeIdeTestSourceSet.groovy.srcDirs)
            testResources.from(smokeIdeTestSourceSet.resources.srcDirs)
        }
    }
}

tasks.register<SmokeIdeTest>("smokeIdeTest") {
    group = "Verification"
    maxParallelForks = 1
    systemProperties["org.gradle.integtest.executer"] = "forking"
    testClassesDirs = smokeIdeTestSourceSet.output.classesDirs
    classpath = smokeIdeTestSourceSet.runtimeClasspath
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
    jvmArgumentProviders.add(
        SmokeIdeTestSystemProperties(
            gradleProperty("ideaHome"),
            gradleProperty("studioHome")
        )
    )
}

tasks.withType<GroovyCompile>().configureEach {
    options.release = 17
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

class SmokeIdeTestSystemProperties(
    @get:Internal
    val ideaHome: Provider<String>,

    @get:Internal
    val studioHome: Provider<String>
) : CommandLineArgumentProvider {
    override fun asArguments(): MutableIterable<String> = buildList {
        if (ideaHome.isPresent) {
            add("-DideaHome=${ideaHome.get()}")
        }
        if (studioHome.isPresent) {
            add("-DstudioHome=${studioHome.get()}")
        }
    }.toMutableList()
}

dependencies {
    smokeIdeTestImplementation("com.jetbrains.intellij.tools:ide-starter-squashed:232.10300.40") {
        exclude("io.grpc")
    }
    smokeIdeTestImplementation("com.jetbrains.intellij.tools:ide-starter-junit4:232.10300.40") {
        exclude("io.grpc")
    }
    smokeIdeTestImplementation("com.jetbrains.intellij.tools:ide-performance-testing-commands:232.10300.40") {
        exclude("io.grpc")
    }
    smokeIdeTestImplementation("org.kodein.di:kodein-di-jvm:7.16.0")
    smokeIdeTestImplementation(libs.gradleProfiler)
    smokeIdeTestDistributionRuntimeOnly(project(":distributions-full")) {
        because("Tests starts an IDE with using current Gradle distribution")
    }
}
