plugins {
    `java-library`
}

description = "Ledger Core - Economy API implementation for Hytale servers"

dependencies {
    // API module
    api(project(":api"))

    // Hytale Server API (compile-only - provided at runtime)
    compileOnly(files("../libs/hytale-server.jar"))

    // Configuration (TOML/JSON) - provided by Hytale at runtime
    compileOnly("com.google.code.gson:gson:2.10.1")

    // Database
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // Annotations
    compileOnly("org.jetbrains:annotations:24.1.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation(files("../libs/hytale-server.jar"))
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Ledger",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "KarmaByte"
        )
    }

    // Include dependencies in jar for distribution
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .filter { !it.name.contains("hytale") }
            .map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Copy>("copyToServer") {
    from(tasks.jar)
    into(file("${rootProject.projectDir}/../server/mods"))
    description = "Copy jar to local server mods folder"
    group = "deployment"
}
