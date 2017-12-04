package io.github.notapresent.usersampler.common;

/* Marker interface to designate user status enums */
public interface Status {
    /* Mimic enum values */
    String name();
    int ordinal();

    default int toInt() {
        int genericSize = GenericStatus.values().length;
        if (this instanceof GenericStatus) {
            return ordinal();
        } else {
            return genericSize + ordinal();
        }
    }
}
