package org.nanocontext.annotationsprocessor;

public enum ScopeType {

    PROTOTYPE("prototype"),
    SINGLETON("singleton");

    private final String value;

    ScopeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScopeType fromValue(String v) {
        for (ScopeType c: ScopeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
