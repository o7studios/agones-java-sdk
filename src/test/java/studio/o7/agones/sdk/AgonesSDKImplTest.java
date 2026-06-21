package studio.o7.agones.sdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgonesSDKImplTest {
    private TestServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.close();
        }
    }

    @Test
    void simpleStableMethodsPostToExpectedEndpoints() throws IOException {
        server = TestServer.withResponses(
                TestResponse.ok("{}"),
                TestResponse.ok("{}"),
                TestResponse.ok("{}")
        );
        AgonesSDK sdk = new AgonesSDKImpl(server.url());

        sdk.allocate();
        sdk.health();
        sdk.shutdown();

        List<TestRequest> requests = server.requests();
        assertEquals(3, requests.size());
        assertEquals("POST", requests.get(0).method());
        assertEquals("/allocate", requests.get(0).path());
        assertEquals("POST", requests.get(1).method());
        assertEquals("/health", requests.get(1).path());
        assertEquals("POST", requests.get(2).method());
        assertEquals("/shutdown", requests.get(2).path());
    }

    @Test
    void readyPostsToReadyEndpoint() throws IOException {
        server = TestServer.ok("{}");

        new AgonesSDKImpl(server.url()).ready();

        TestRequest request = server.singleRequest();
        assertEquals("POST", request.method());
        assertEquals("/ready", request.path());
        assertEquals("{}", request.body());
    }

    @Test
    void deallocateTransitionsServerBackToReady() throws IOException {
        server = TestServer.ok("{}");

        new AgonesSDKImpl(server.url()).deallocate();

        TestRequest request = server.singleRequest();
        assertEquals("POST", request.method());
        assertEquals("/ready", request.path());
        assertEquals("{}", request.body());
    }

    @Test
    void gameServerReturnsParsedGameServerObject() throws IOException {
        server = TestServer.ok("""
                {
                  "object_meta": {
                    "name": "simple-game-server",
                    "namespace": "default",
                    "labels": {"mode": "deathmatch"},
                    "annotations": {"region": "eu"}
                  },
                  "spec": {
                    "ports": [
                      {
                        "name": "default",
                        "protocol": "UDP",
                        "containerPort": 7654,
                        "hostPort": 7777
                      }
                    ]
                  },
                  "status": {
                    "state": "Allocated",
                    "address": "203.0.113.10",
                    "ports": [
                      {"name": "default", "port": 7777},
                      {"name": "metrics", "port": 9090}
                    ]
                  }
                }
                """);

        GameServer gameServer = new AgonesSDKImpl(server.url()).gameServer();

        assertEquals("simple-game-server", gameServer.getObjectMeta().getName());
        assertEquals("default", gameServer.getObjectMeta().getNamespace());
        assertEquals("deathmatch", gameServer.getObjectMeta().getLabels().get("mode"));
        assertEquals("eu", gameServer.getObjectMeta().getAnnotations().get("region"));
        assertEquals("Allocated", gameServer.getStatus().getState());
        assertEquals("203.0.113.10", gameServer.getHost());
        assertEquals(7777, gameServer.getPort());
        assertEquals(9090, gameServer.getStatus().getPort("metrics"));
        assertEquals(7654, gameServer.getSpec().getPorts().getFirst().getContainerPort());

        TestRequest request = server.singleRequest();
        assertEquals("GET", request.method());
        assertEquals("/gameserver", request.path());
    }

    @Test
    void reserveSendsSecondsPayload() throws IOException {
        server = TestServer.ok("{}");

        new AgonesSDKImpl(server.url()).reserve(30);

        TestRequest request = server.singleRequest();
        assertEquals("POST", request.method());
        assertEquals("/reserve", request.path());
        assertEquals("{\"seconds\": \"30\"}", request.body());
    }

    @Test
    void setAnnotationSendsMetadataPayload() throws IOException {
        server = TestServer.ok("{}");

        new AgonesSDKImpl(server.url()).setAnnotation("match-id", "abc123");

        TestRequest request = server.singleRequest();
        assertEquals("PUT", request.method());
        assertEquals("/metadata/annotation", request.path());
        assertEquals("{\"key\": \"match-id\", \"value\": \"abc123\"}", request.body());
    }

    @Test
    void setLabelSendsMetadataPayload() throws IOException {
        server = TestServer.ok("{}");

        new AgonesSDKImpl(server.url()).setLabel("map", "arena");

        TestRequest request = server.singleRequest();
        assertEquals("PUT", request.method());
        assertEquals("/metadata/label", request.path());
        assertEquals("{\"key\": \"map\", \"value\": \"arena\"}", request.body());
    }

    @Test
    void failedSdkRequestThrowsIOException() {
        server = TestServer.withResponse(500, "{}");

        IOException exception = assertThrows(
                IOException.class,
                () -> new AgonesSDKImpl(server.url()).allocate()
        );

        assertEquals("Allocate request failed: 500 - Internal Server Error", exception.getMessage());
    }

    @Test
    void betaCounterOperationsReadThenPatchCounter() throws IOException {
        server = TestServer.withResponses(
                TestResponse.ok("{\"count\": 2, \"capacity\": 10}"),
                TestResponse.ok("{}")
        );

        new AgonesSDKImpl(server.url()).beta().incrementCounter("players", 3);

        List<TestRequest> requests = server.requests();
        assertEquals(2, requests.size());
        assertEquals("GET", requests.get(0).method());
        assertEquals("/v1beta1/counters/players", requests.get(0).path());
        assertEquals("PATCH", requests.get(1).method());
        assertEquals("/v1beta1/counters/players", requests.get(1).path());
        assertEquals("{\"count\": \"5\", \"capacity\": \"10\"}", requests.get(1).body());
    }

    private record TestRequest(String method, String path, String body) {
    }

    private record TestResponse(int status, String body) {
        static TestResponse ok(String body) {
            return new TestResponse(200, body);
        }
    }

    private static final class TestServer implements AutoCloseable {
        private final HttpServer server;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final List<TestRequest> requests = new ArrayList<>();
        private final List<TestResponse> responses = new ArrayList<>();

        private TestServer(List<TestResponse> responses) {
            this.responses.addAll(responses);
            try {
                server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
            server.createContext("/", this::handle);
            server.setExecutor(executor);
            server.start();
        }

        static TestServer ok(String body) {
            return withResponse(200, body);
        }

        static TestServer withResponse(int status, String body) {
            return new TestServer(List.of(new TestResponse(status, body)));
        }

        static TestServer withResponses(TestResponse... responses) {
            return new TestServer(List.of(responses));
        }

        String url() {
            return "http://localhost:" + server.getAddress().getPort() + "/";
        }

        TestRequest singleRequest() {
            assertEquals(1, requests.size());
            return requests.getFirst();
        }

        List<TestRequest> requests() {
            return requests;
        }

        private void handle(HttpExchange exchange) throws IOException {
            byte[] body = exchange.getRequestBody().readAllBytes();
            requests.add(new TestRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    new String(body, StandardCharsets.UTF_8)
            ));

            TestResponse response = responses.size() > 1 ? responses.removeFirst() : responses.getFirst();
            byte[] responseBody = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.status(), responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.close();
        }

        @Override
        public void close() {
            server.stop(0);
            executor.shutdownNow();
        }
    }
}
