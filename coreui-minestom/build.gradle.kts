dependencies {
    "api"(project(":coreui-core"))
    "api"(project(":coreui-pack"))
    "compileOnly"("net.minestom:minestom:2026.10.07-26.2")
}
tasks.withType<JavaCompile>().configureEach { options.release = 25 }
