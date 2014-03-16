package com.goironbox.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Symmetric key strengths.
 * 
 * @since   2.0
 */
public enum SymmetricKeyStrength {

    /**
     * None.
     */
    NONE("0"),

    /**
     * AES-128.
     */
    AES_128("1"),

    /**
     * AES-256.
     */
    AES_256("2");
    
    private static final Map<String, SymmetricKeyStrength> lookup = new HashMap<>();

    static {
        for (SymmetricKeyStrength sks : EnumSet.allOf(SymmetricKeyStrength.class)) {
            lookup.put(sks.getRESTString(), sks);
        }
    }

    private final String restString;

    private SymmetricKeyStrength(String s) {
        restString = s;
    }

    static SymmetricKeyStrength getFromRESTString(String restString) {
        return lookup.get(restString);
    }

    String getRESTString() {
        return restString;
    }

}
