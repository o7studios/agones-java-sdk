import net.thebugmc.gradle.sonatypepublisher.PublishingType

plugins {
    id("studio.o7.remora") version "0.0.9"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
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

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
    name = remora.artifactId
    publishingType = PublishingType.USER_MANAGED
}

signing {
    useInMemoryPgpKeys(System.getenv("GPG_KEY"), System.getenv("GPG_PASSPHRASE"))
    sign(publishing.publications)
}
