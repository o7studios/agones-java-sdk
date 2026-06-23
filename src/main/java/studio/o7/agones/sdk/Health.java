package studio.o7.agones.sdk;

public final class Health {
    private Boolean disabled;
    private Integer periodSeconds;
    private Integer failureThreshold;
    private Integer initialDelaySeconds;

    public Boolean getDisabled() {
        return disabled;
    }

    public Integer getPeriodSeconds() {
        return periodSeconds;
    }

    public Integer getFailureThreshold() {
        return failureThreshold;
    }

    public Integer getInitialDelaySeconds() {
        return initialDelaySeconds;
    }
}
