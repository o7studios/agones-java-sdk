package studio.o7.agones.sdk;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public final class ObjectMeta {
    private String name;
    private String generateName;
    private String namespace;
    private String uid;
    @SerializedName(value = "resource_version", alternate = "resourceVersion")
    private String resourceVersion;
    private Long generation;
    @SerializedName(value = "creation_timestamp", alternate = "creationTimestamp")
    private String creationTimestamp;
    @SerializedName(value = "deletion_timestamp", alternate = "deletionTimestamp")
    private String deletionTimestamp;
    private Long deletionGracePeriodSeconds;
    private Map<String, String> annotations;
    private Map<String, String> labels;
    private List<String> finalizers;
    private List<Map<String, Object>> ownerReferences;
    private List<Map<String, Object>> managedFields;

    public String getName() {
        return name;
    }

    public String getGenerateName() {
        return generateName;
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

    public Long getDeletionGracePeriodSeconds() {
        return deletionGracePeriodSeconds;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public List<String> getFinalizers() {
        return finalizers;
    }

    public List<Map<String, Object>> getOwnerReferences() {
        return ownerReferences;
    }

    public List<Map<String, Object>> getManagedFields() {
        return managedFields;
    }
}
