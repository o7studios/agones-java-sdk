package studio.o7.agones.sdk;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public final class GameServer {
    @SerializedName("object_meta")
    private ObjectMeta objectMeta;
    private Spec spec;
    private Status status;

    public ObjectMeta getObjectMeta() {
        return objectMeta;
    }

    public Spec getSpec() {
        return spec;
    }

    public Status getStatus() {
        return status;
    }

    public String getHost() {
        return status == null ? null : status.getAddress();
    }

    public Integer getPort() {
        if (status == null || status.getPorts() == null || status.getPorts().isEmpty()) {
            return null;
        }
        return status.getPorts().getFirst().getPort();
    }

    public static final class ObjectMeta {
        private String name;
        private String namespace;
        private String uid;
        @SerializedName("resource_version")
        private String resourceVersion;
        private Long generation;
        @SerializedName("creation_timestamp")
        private String creationTimestamp;
        @SerializedName("deletion_timestamp")
        private String deletionTimestamp;
        private Map<String, String> annotations;
        private Map<String, String> labels;

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getUid() {
            return uid;
        }

        public String getResourceVersion() {
            return resourceVersion;
        }

        public Long getGeneration() {
            return generation;
        }

        public String getCreationTimestamp() {
            return creationTimestamp;
        }

        public String getDeletionTimestamp() {
            return deletionTimestamp;
        }

        public Map<String, String> getAnnotations() {
            return annotations;
        }

        public Map<String, String> getLabels() {
            return labels;
        }
    }

    public static final class Spec {
        private Map<String, Object> health;
        private Map<String, Object> sdkServer;
        private List<SpecPort> ports;

        public Map<String, Object> getHealth() {
            return health;
        }

        public Map<String, Object> getSdkServer() {
            return sdkServer;
        }

        public List<SpecPort> getPorts() {
            return ports;
        }
    }

    public static final class SpecPort {
        private String name;
        private String protocol;
        private String portPolicy;
        private Integer containerPort;
        private Integer hostPort;

        public String getName() {
            return name;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getPortPolicy() {
            return portPolicy;
        }

        public Integer getContainerPort() {
            return containerPort;
        }

        public Integer getHostPort() {
            return hostPort;
        }
    }

    public static final class Status {
        private String state;
        private String address;
        private String nodeName;
        private List<StatusPort> ports;
        private Map<String, Object> counters;
        private Map<String, Object> lists;
        private Map<String, Object> players;
        private String reservedUntil;

        public String getState() {
            return state;
        }

        public String getAddress() {
            return address;
        }

        public String getNodeName() {
            return nodeName;
        }

        public List<StatusPort> getPorts() {
            return ports;
        }

        public Integer getPort(@lombok.NonNull String name) {
            if (ports == null) {
                return null;
            }
            return ports.stream()
                    .filter(port -> name.equals(port.getName()))
                    .findFirst()
                    .map(StatusPort::getPort)
                    .orElse(null);
        }

        public Map<String, Object> getCounters() {
            return counters;
        }

        public Map<String, Object> getLists() {
            return lists;
        }

        public Map<String, Object> getPlayers() {
            return players;
        }

        public String getReservedUntil() {
            return reservedUntil;
        }
    }

    public static final class StatusPort {
        private String name;
        private Integer port;

        public String getName() {
            return name;
        }

        public Integer getPort() {
            return port;
        }
    }
}
