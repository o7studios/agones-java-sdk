package studio.o7.agones.sdk;

public final class GameServerPort {
    private String name;
    private String range;
    private String portPolicy;
    private String container;
    private Integer containerPort;
    private Integer hostPort;
    private String protocol;

    public String getName() {
        return name;
    }

    public String getRange() {
        return range;
    }

    public String getPortPolicy() {
        return portPolicy;
    }

    public String getContainer() {
        return container;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public String getProtocol() {
        return protocol;
    }
}
