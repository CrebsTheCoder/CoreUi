plugins { id("xyz.jpenilla.run-paper") }
dependencies {
    "api"(project(":coreui-core"))
    "api"(project(":coreui-pack"))
    "compileOnly"("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}
tasks {
    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
    processResources {
        inputs.property("version", project.version)
        filesMatching("plugin.yml") { expand("version" to project.version) }
    }
    jar {
        archiveBaseName = "CoreUi"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(configurations.runtimeClasspath)
        from(provider {
            configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
                .filter { it.moduleVersion.id.group !in setOf("net.kyori", "org.jetbrains", "org.jspecify") }
                .map { if (it.file.isDirectory) it.file else zipTree(it.file) }
        })
        exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    }
}
