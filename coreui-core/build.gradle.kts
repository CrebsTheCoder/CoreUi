dependencies {
    "api"(project(":coreui-api"))
    "implementation"("net.kyori:adventure-text-serializer-plain:4.25.0")
    "compileOnly"("org.jspecify:jspecify:1.0.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.4")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}
tasks.withType<Test>().configureEach { useJUnitPlatform() }
