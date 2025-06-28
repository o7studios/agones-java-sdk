package studio.o7.agones.sdk;

import lombok.NonNull;
import studio.o7.agones.sdk.beta.AgonesBeta;

import java.io.IOException;

public interface AgonesSDK {

    static AgonesSDK NewSDK() {
        return new AgonesSDKImpl();
    }

    void allocate() throws IOException;

    AgonesBeta beta();

    void health() throws IOException;

    void ready() throws IOException;

    void reserve(int seconds) throws IOException;

    void setAnnotation(@NonNull String key, @NonNull String value) throws IOException;

    void setLabel(@NonNull String key, @NonNull String value) throws IOException;

    void shutdown() throws IOException;
}
