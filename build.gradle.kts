import net.thebugmc.gradle.sonatypepublisher.PublishingType

plugins {
    id("studio.o7.remora") version "0.0.9"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

remora {
    group = "studio.o7"
    artifactId = "agones-sdk"

    description = "Agones Java gRPC SDK implementation"
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.13.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
    name = remora.artifactId
    publishingType = PublishingType.USER_MANAGED

    pom {
        name = remora.artifactId
        url = "https://github.com/o7studios/agones-java-sdk"
        description = remora.description

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
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("GPG_KEY"), System.getenv("GPG_PASSPHRASE"))
    sign(publishing.publications)
}