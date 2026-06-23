package studio.o7.agones.sdk;

import java.util.List;

public final class PlayerStatus {
    private Long count;
    private Long capacity;
    private List<String> ids;

    public Long getCount() {
        return count;
    }

    public Long getCapacity() {
        return capacity;
    }

    public List<String> getIds() {
        return ids;
    }
}
