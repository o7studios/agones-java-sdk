package studio.o7.agones.sdk;

import com.google.gson.annotations.SerializedName;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public final class GameServerStatus {
    private String state;
    private List<GameServerStatusPort> ports;
    private String address;
    private List<NodeAddress> addresses;
    @SerializedName(value = "nodeName", alternate = "node_name")
    private String nodeName;
    @SerializedName(value = "reservedUntil", alternate = "reserved_until")
    private String reservedUntil;
    private PlayerStatus players;
    private Map<String, CounterStatus> counters;
    private Map<String, ListStatus> lists;
    private Eviction eviction;

    public String getState() {
        return state;
    }

    public List<GameServerStatusPort> getPorts() {
        return ports;
    }

    public String getAddress() {
        return address;
    }

    public List<NodeAddress> getAddresses() {
        return addresses;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getReservedUntil() {
        return reservedUntil;
    }

    public PlayerStatus getPlayers() {
        return players;
    }

    public Map<String, CounterStatus> getCounters() {
        return counters;
    }

    public Map<String, ListStatus> getLists() {
        return lists;
    }

    public Eviction getEviction() {
        return eviction;
    }

    public Integer getPort(@NonNull String name) {
        if (ports == null) {
            return null;
        }
        return ports.stream()
                .filter(port -> name.equals(port.getName()))
                .findFirst()
                .map(GameServerStatusPort::getPort)
                .orElse(null);
    }

    public String getAddress(@NonNull String type) {
        if (addresses == null) {
            return null;
        }
        return addresses.stream()
                .filter(address -> type.equals(address.getType()))
                .findFirst()
                .map(NodeAddress::getAddress)
                .orElse(null);
    }
}
