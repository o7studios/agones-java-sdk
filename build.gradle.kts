plugins {
    id("studio.o7.remora") version "0.1.6"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

group = "studio.o7"

information {
    description = "Agones Java gRPC SDK implementation"
    url = "https://github.com/o7studios/agones-java-sdk"

    developers {
        developer {
            id = "julian-siebert"
            name = "Julian Siebert"
            email = "mail@julian-siebert.de"
        }
    }

    scm {
        connection = "scm:git:git://github.com/o7studios/agones-java-sdk.git"
        developerConnection = "scm:git:git@https://github.com/o7studios/agones-java-sdk.git"
        url = "https://github.com/o7studios/agones-java-sdk"
        tag = "HEAD"
    }

    ciManagement {
        system = "GitHub Actions"
        url = "https://github.com/o7studios/agones-java-sdk/actions"
    }

    licenses {
        license {
            name = "The Apache Software License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.13.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))