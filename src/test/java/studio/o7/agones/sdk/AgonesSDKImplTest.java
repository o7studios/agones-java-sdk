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
                  "apiVersion": "agones.dev/v1",
                  "kind": "GameServer",
                  "object_meta": {
                    "name": "simple-game-server",
                    "namespace": "default",
                    "uid": "uid-123",
                    "resource_version": "42",
                    "generation": 3,
                    "creation_timestamp": "2026-06-23T10:00:00Z",
                    "deletionGracePeriodSeconds": 30,
                    "labels": {"mode": "deathmatch"},
                    "annotations": {"region": "eu"},
                    "finalizers": ["agones.dev/finalizer"],
                    "ownerReferences": [{"name": "fleet-a"}],
                    "managedFields": [{"manager": "controller"}]
                  },
                  "spec": {
                    "container": "game",
                    "ports": [
                      {
                        "name": "default",
                        "range": "default",
                        "portPolicy": "Dynamic",
                        "container": "game",
                        "protocol": "UDP",
                        "containerPort": 7654,
                        "hostPort": 7777
                      }
                    ],
                    "health": {
                      "disabled": false,
                      "periodSeconds": 5,
                      "failureThreshold": 3,
                      "initialDelaySeconds": 10
                    },
                    "scheduling": "Packed",
                    "sdkServer": {
                      "logLevel": "Info",
                      "grpcPort": 9357,
                      "httpPort": 9358
                    },
                    "template": {
                      "spec": {
                        "restartPolicy": "Never"
                      }
                    },
                    "players": {
                      "initialCapacity": 10
                    },
                    "counters": {
                      "rooms": {"count": 1, "capacity": 5}
                    },
                    "lists": {
                      "maps": {"capacity": 3, "values": ["arena"]}
                    },
                    "eviction": {
                      "safe": "OnUpgrade"
                    }
                  },
                  "status": {
                    "state": "Allocated",
                    "address": "203.0.113.10",
                    "addresses": [
                      {"type": "InternalIP", "address": "10.0.0.10"},
                      {"type": "ExternalIP", "address": "203.0.113.10"}
                    ],
                    "ports": [
                      {"name": "default", "port": 7777},
                      {"name": "metrics", "port": 9090}
                    ],
                    "nodeName": "node-a",
                    "reservedUntil": "2026-06-23T11:00:00Z",
                    "players": {
                      "count": 2,
                      "capacity": 10,
                      "ids": ["player-1", "player-2"]
                    },
                    "counters": {
                      "rooms": {"count": 2, "capacity": 5}
                    },
                    "lists": {
                      "maps": {"capacity": 3, "values": ["arena", "castle"]}
                    },
                    "eviction": {
                      "safe": "Always"
                    }
                  }
                }
                """);

        GameServer gameServer = new AgonesSDKImpl(server.url()).gameServer();

        assertEquals("agones.dev/v1", gameServer.getApiVersion());
        assertEquals("GameServer", gameServer.getKind());
        assertEquals("simple-game-server", gameServer.getObjectMeta().getName());
        assertEquals(gameServer.getObjectMeta(), gameServer.getMetadata());
        assertEquals("default", gameServer.getObjectMeta().getNamespace());
        assertEquals("uid-123", gameServer.getObjectMeta().getUid());
        assertEquals("42", gameServer.getObjectMeta().getResourceVersion());
        assertEquals(3, gameServer.getObjectMeta().getGeneration());
        assertEquals("2026-06-23T10:00:00Z", gameServer.getObjectMeta().getCreationTimestamp());
        assertEquals(30, gameServer.getObjectMeta().getDeletionGracePeriodSeconds());
        assertEquals("deathmatch", gameServer.getObjectMeta().getLabels().get("mode"));
        assertEquals("eu", gameServer.getObjectMeta().getAnnotations().get("region"));
        assertEquals("agones.dev/finalizer", gameServer.getObjectMeta().getFinalizers().getFirst());
        assertEquals("fleet-a", gameServer.getObjectMeta().getOwnerReferences().getFirst().get("name"));
        assertEquals("controller", gameServer.getObjectMeta().getManagedFields().getFirst().get("manager"));
        assertEquals("game", gameServer.getSpec().getContainer());
        assertEquals("default", gameServer.getSpec().getPorts().getFirst().getRange());
        assertEquals("Dynamic", gameServer.getSpec().getPorts().getFirst().getPortPolicy());
        assertEquals("game", gameServer.getSpec().getPorts().getFirst().getContainer());
        assertEquals("UDP", gameServer.getSpec().getPorts().getFirst().getProtocol());
        assertEquals(7654, gameServer.getSpec().getPorts().getFirst().getContainerPort());
        assertEquals(7777, gameServer.getSpec().getPorts().getFirst().getHostPort());
        assertEquals(false, gameServer.getSpec().getHealth().getDisabled());
        assertEquals(5, gameServer.getSpec().getHealth().getPeriodSeconds());
        assertEquals(3, gameServer.getSpec().getHealth().getFailureThreshold());
        assertEquals(10, gameServer.getSpec().getHealth().getInitialDelaySeconds());
        assertEquals("Packed", gameServer.getSpec().getScheduling());
        assertEquals("Info", gameServer.getSpec().getSdkServer().getLogLevel());
        assertEquals(9357, gameServer.getSpec().getSdkServer().getGrpcPort());
        assertEquals(9358, gameServer.getSpec().getSdkServer().getHttpPort());
        assertEquals("Never", ((java.util.Map<?, ?>) gameServer.getSpec().getTemplate().get("spec")).get("restartPolicy"));
        assertEquals(10, gameServer.getSpec().getPlayers().getInitialCapacity());
        assertEquals(1, gameServer.getSpec().getCounters().get("rooms").getCount());
        assertEquals(5, gameServer.getSpec().getCounters().get("rooms").getCapacity());
        assertEquals(3, gameServer.getSpec().getLists().get("maps").getCapacity());
        assertEquals("arena", gameServer.getSpec().getLists().get("maps").getValues().getFirst());
        assertEquals("OnUpgrade", gameServer.getSpec().getEviction().getSafe());
        assertEquals("Allocated", gameServer.getStatus().getState());
        assertEquals("203.0.113.10", gameServer.getHost());
        assertEquals("InternalIP", gameServer.getStatus().getAddresses().getFirst().getType());
        assertEquals("10.0.0.10", gameServer.getStatus().getAddresses().getFirst().getAddress());
        assertEquals("node-a", gameServer.getStatus().getNodeName());
        assertEquals("2026-06-23T11:00:00Z", gameServer.getStatus().getReservedUntil());
        assertEquals(2, gameServer.getStatus().getPlayers().getCount());
        assertEquals(10, gameServer.getStatus().getPlayers().getCapacity());
        assertEquals("player-1", gameServer.getStatus().getPlayers().getIds().getFirst());
        assertEquals(2, gameServer.getStatus().getCounters().get("rooms").getCount());
        assertEquals(5, gameServer.getStatus().getCounters().get("rooms").getCapacity());
        assertEquals(3, gameServer.getStatus().getLists().get("maps").getCapacity());
        assertEquals("castle", gameServer.getStatus().getLists().get("maps").getValues().get(1));
        assertEquals("Always", gameServer.getStatus().getEviction().getSafe());
        assertEquals(7777, gameServer.getPort());
        assertEquals(9090, gameServer.getStatus().getPort("metrics"));

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
