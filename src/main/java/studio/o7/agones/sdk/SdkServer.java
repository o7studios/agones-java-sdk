package studio.o7.agones.sdk;

import com.google.gson.annotations.SerializedName;

public final class SdkServer {
    private String logLevel;
    @SerializedName(value = "grpcPort", alternate = "grpc_port")
    private Integer grpcPort;
    @SerializedName(value = "httpPort", alternate = "http_port")
    private Integer httpPort;

    public String getLogLevel() {
        return logLevel;
    }

    public Integer getGrpcPort() {
        return grpcPort;
    }

    public Integer getHttpPort() {
        return httpPort;
    }
}
