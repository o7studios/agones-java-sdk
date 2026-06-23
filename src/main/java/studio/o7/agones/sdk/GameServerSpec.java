package studio.o7.agones.sdk;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public final class GameServerSpec {
    private String container;
    private List<GameServerPort> ports;
    private Health health;
    private String scheduling;
    @SerializedName(value = "sdkServer", alternate = "sdk_server")
    private SdkServer sdkServer;
    private Map<String, Object> template;
    private PlayersSpec players;
    private Map<String, CounterStatus> counters;
    private Map<String, ListStatus> lists;
    private Eviction eviction;

    public String getContainer() {
        return container;
    }

    public List<GameServerPort> getPorts() {
        return ports;
    }

    public Health getHealth() {
        return health;
    }

    public String getScheduling() {
        return scheduling;
    }

    public SdkServer getSdkServer() {
        return sdkServer;
    }

    public Map<String, Object> getTemplate() {
        return template;
    }

    public PlayersSpec getPlayers() {
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
}
