package studio.o7.agones.sdk;

import com.google.gson.annotations.SerializedName;

public final class GameServer {
    private String apiVersion;
    private String kind;
    @SerializedName(value = "object_meta", alternate = "metadata")
    private ObjectMeta objectMeta;
    private GameServerSpec spec;
    private GameServerStatus status;

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public ObjectMeta getObjectMeta() {
        return objectMeta;
    }

    public ObjectMeta getMetadata() {
        return objectMeta;
    }

    public GameServerSpec getSpec() {
        return spec;
    }

    public GameServerStatus getStatus() {
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
}
