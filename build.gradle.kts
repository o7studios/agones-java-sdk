
plugins {
    id("studio.o7.remora") version "0.0.9"
}

remora {
    group = "studio.o7"
    artifactId = "agones-sdk"

    description = "Agones Java gRPC SDK implementation"

    framework {
        lombok.isEnabled = true
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.13.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "studio.o7"
            artifactId = "agones-sdk"
            version = System.getenv("RELEASE_VERSION") ?: "unspecified"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/o7studios/agones-java-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}