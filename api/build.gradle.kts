plugins {
    java
}

description = "Ledger API - Economy API for Hytale servers"

dependencies {
    // Hytale Server API (compile-only - provided at runtime)
    // Download from: https://hytale.com/developers or copy from server installation
    compileOnly(files("../libs/hytale-server.jar"))

    // Annotations
    compileOnly("org.jetbrains:annotations:24.1.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Ledger API",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "KarmaByte"
        )
    }
}
