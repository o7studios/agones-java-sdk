package studio.o7.agones.sdk.beta;

import lombok.NonNull;

import java.io.IOException;
import java.util.Collection;

public interface AgonesBeta {

    void decrementCounter(@NonNull String key, long amount) throws IOException;

    long getCounterCapacity(@NonNull String key) throws IOException;

    long getCounterCount(@NonNull String key) throws IOException;

    void incrementCounter(@NonNull String key, long amount) throws IOException;

    void setCounterCapacity(@NonNull String key, long amount) throws IOException;

    void setCounterCount(@NonNull String key, long amount) throws IOException;
}
