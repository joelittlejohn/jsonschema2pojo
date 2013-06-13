
package com.googlecode.jsonschema2pojo.integration.generation.gsonit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;


/**
 * schema for an fstab entry
 * 
 */
@Generated("com.googlecode.jsonschema2pojo")
public class FstabEntry {

    private Storage storage;
    @SerializedName("fs_type")
    private FstabEntry.FsType fsType;
    private Set<String> options = new HashSet<String>();
    @SerializedName("read_only")
    private Boolean readOnly;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public FstabEntry.FsType getFsType() {
        return fsType;
    }

    public void setFsType(FstabEntry.FsType fsType) {
        this.fsType = fsType;
    }

    public Set<String> getOptions() {
        return options;
    }

    public void setOptions(Set<String> options) {
        this.options = options;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Generated("com.googlecode.jsonschema2pojo")
    public static enum FsType {

        EXT_3("ext3"),
        EXT_4("ext4"),
        BTRFS("btrfs");
        private final String value;
        private static Map<String, FstabEntry.FsType> constants = new HashMap<String, FstabEntry.FsType>();

        static {
            for (FstabEntry.FsType c: FstabEntry.FsType.values()) {
                constants.put(c.value, c);
            }
        }

        private FsType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static FstabEntry.FsType fromValue(String value) {
            FstabEntry.FsType constant = constants.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
