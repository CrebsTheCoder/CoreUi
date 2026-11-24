dependencies {
    "api"(project(":coreui-api"))
    "api"("com.github.DRAGNIL68:Packer:d7bb7af0cf")
    "implementation"("com.google.code.gson:gson:2.11.0")
    "implementation"("org.yaml:snakeyaml:2.2")
    "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.4")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}
tasks.withType<Test>().configureEach { useJUnitPlatform() }
