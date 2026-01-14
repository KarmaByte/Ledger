plugins {
    java
    `maven-publish`
}

allprojects {
    group = property("group") as String
    version = property("version") as String

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        val javaVersionNum = (property("javaVersion") as String).toInt()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersionNum))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf(
            "-parameters",
            "-Xlint:all",
            "-Xlint:-processing"
        ))
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            links("https://docs.oracle.com/en/java/javase/25/docs/api/")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set(property("description") as String)
                    url.set("https://github.com/KarmaByte/Ledger")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("karmabyte")
                            name.set("KarmaByte")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/KarmaByte/Ledger.git")
                        developerConnection.set("scm:git:ssh://github.com/KarmaByte/Ledger.git")
                        url.set("https://github.com/KarmaByte/Ledger")
                    }
                }
            }
        }
    }
}

tasks.register("buildAll") {
    dependsOn(subprojects.map { it.tasks.named("build") })
    description = "Build all modules"
    group = "build"
}
