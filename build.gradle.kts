plugins {
    base
    id("xyz.jpenilla.run-paper") version "3.1.0" apply false
}
subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    group = rootProject.group
    version = rootProject.version
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
        withSourcesJar()
    }
    tasks.withType<JavaCompile>().configureEach { options.release = 21 }
    extensions.configure<PublishingExtension> {
        publications { create<MavenPublication>("mavenJava") { from(components["java"]) } }
    }
}
tasks.named("build") { dependsOn(subprojects.map { it.path + ":build" }) }
tasks.named("clean") { dependsOn(subprojects.map { it.path + ":clean" }) }
tasks.register("runServer") { dependsOn(":coreui-paper:runServer") }
