plugins {
    java
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor(libs.jabel)
    compileOnly(libs.jabel)
}

tasks {
    compileJava {
        sourceCompatibility = "21"
        options.compilerArgs.add("-Xlint:-options")
        options.release.set(8)
    }
    jar {
        manifest.attributes("Main-Class" to "${project.group}.MagicConchShell")
    }
}
