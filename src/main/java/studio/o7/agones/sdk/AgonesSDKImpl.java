package studio.o7.agones.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import studio.o7.agones.sdk.beta.AgonesBeta;

import java.io.IOException;

final class AgonesSDKImpl implements AgonesSDK {
    private static final String DEFAULT_URL = "http://localhost:9358/";
    private static final Gson GSON = new GsonBuilder().create();

    private final String url = getUrl();
    private final OkHttpClient client = new OkHttpClient();

    private final Request allocateRequest = new Request.Builder()
            .url(url + "allocate")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("{}", MediaType.get("application/json")))
            .build();

    @Override
    public void allocate() throws IOException {
        try (Response response = client.newCall(allocateRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Allocate request failed: " + code + " - " + message);
        }
    }

    private final Request healthRequest = new Request.Builder()
            .url(url + "health")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("{}", MediaType.get("application/json")))
            .build();

    @Override
    public void health() throws IOException {
        try (Response response = client.newCall(healthRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Health request failed: " + code + " - " + message);
        }
    }

    private final Request readyRequest = new Request.Builder()
            .url(url + "ready")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("{}", MediaType.get("application/json")))
            .build();

    @Override
    public void ready() throws IOException {
        try (Response response = client.newCall(readyRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Ready request failed: " + code + " - " + message);
        }
    }

    private final Request shutdownRequest = new Request.Builder()
            .url(url + "shutdown")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("{}", MediaType.get("application/json")))
            .build();

    @Override
    public void shutdown() throws IOException {
        try (Response response = client.newCall(shutdownRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Shutdown request failed: " + code + " - " + message);
        }
    }

    @Override
    public void reserve(int seconds) throws IOException {
        Request reserveRequest = new Request.Builder()
                .url(url + "reserve")
                .addHeader("Content-Type", "application/json")
                .post(
                        RequestBody.create(
                                "{\"seconds\": \"" + seconds + "\"}",
                                MediaType.get("application/json")
                        ))
                .build();
        try (Response response = client.newCall(reserveRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Reserve request failed: " + code + " - " + message);
        }
    }

    @Override
    public void setAnnotation(@NonNull String key, @NonNull String value) throws IOException {
        Request annotationRequest = new Request.Builder()
                .url(url + "metadata/annotation")
                .addHeader("Content-Type", "application/json")
                .put(
                        RequestBody.create(
                                "{\"key\": \"" + key + "\", \"value\": \"" + value + "\"}",
                                MediaType.get("application/json")
                        ))
                .build();

        try (Response response = client.newCall(annotationRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Set-Annotation request failed: " + code + " - " + message);
        }
    }

    @Override
    public void setLabel(@NonNull String key, @NonNull String value) throws IOException {
        Request labelRequest = new Request.Builder()
                .url(url + "metadata/label")
                .addHeader("Content-Type", "application/json")
                .put(
                        RequestBody.create(
                                "{\"key\": \"" + key + "\", \"value\": \"" + value + "\"}",
                                MediaType.get("application/json")
                        ))
                .build();

        try (Response response = client.newCall(labelRequest).execute()) {
            if (response.isSuccessful()) return;
            int code = response.code();
            String message = response.message();
            throw new IOException("Set-Label request failed: " + code + " - " + message);
        }
    }

    @Override
    public AgonesBeta beta() {
        return new AgonesBeta() {

            @AllArgsConstructor
            private static class Counter {
                private final String key;
                private long count, capacity;
            }

            @Override
            public void incrementCounter(@NonNull String key, long amount) throws IOException {
                Counter counter = getCounter(key);
                counter.count+=amount;
                updateCounter(counter);
            }

            @Override
            public void decrementCounter(@NonNull String key, long amount) throws IOException {
                Counter counter = getCounter(key);
                counter.count-=amount;
                updateCounter(counter);
            }

            @Override
            public long getCounterCapacity(@NonNull String key) throws IOException {
                return getCounter(key).capacity;
            }

            @Override
            public long getCounterCount(@NonNull String key) throws IOException {
                return getCounter(key).count;
            }

            @Override
            public void setCounterCapacity(@NonNull String key, long amount) throws IOException {
                Counter counter = getCounter(key);
                counter.capacity=amount;
                updateCounter(counter);
            }

            @Override
            public void setCounterCount(@NonNull String key, long amount) throws IOException {
                Counter counter = getCounter(key);
                counter.count=amount;
                updateCounter(counter);
            }

            private void updateCounter(@NonNull Counter counter) throws IOException {
                Request counterRequest = new Request.Builder()
                        .url(url + "v1beta1/counters/" + counter.key)
                        .addHeader("Content-Type", "application/json")
                        .patch(
                                RequestBody.create(
                                        "{\"count\": \"" + counter.count + "\", \"capacity\": \"" + counter.capacity + "\"}",
                                        MediaType.get("application/json")
                                ))
                        .build();

                try (Response response = client.newCall(counterRequest).execute()) {
                    if (response.isSuccessful()) return;
                    int code = response.code();
                    String message = response.message();
                    throw new IOException("Update-Counter request failed: " + code + " - " + message);
                }
            }

            private Counter getCounter(@NonNull String key) throws IOException {
                Request counterRequest = new Request.Builder()
                        .url(url + "v1beta1/counters/" + key)
                        .addHeader("Content-Type", "application/json")
                        .get()
                        .build();

                try (Response response = client.newCall(counterRequest).execute()) {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String bodyString = response.body().string();
                        JsonElement element = JsonParser.parseString(bodyString);
                        JsonObject obj = element.getAsJsonObject();
                        long count = obj.get("count").getAsLong();
                        long capacity = obj.get("capacity").getAsLong();
                        return new Counter(key, count, capacity);
                    }
                    int code = response.code();
                    String message = response.message();
                    throw new IOException("Get-Counter request failed: " + code + " - " + message);
                }
            }
        };
    }

    private String getUrl() {
        String port = System.getenv("AGONES_SDK_HTTP_PORT");
        if (port == null || port.isEmpty()) return DEFAULT_URL;
        return "http://localhost:" + port + "/";
    }
}
