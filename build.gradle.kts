plugins {
    id("studio.o7.remora") version "0.0.9"
}

remora {
    group = "studio.o7"
    artifactId = "agones-sdk"

    description = "Agones Java gRPC SDK implementation"

    mavenCentral.isEnabled = true

    framework {
        lombok.isEnabled = true
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.13.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
