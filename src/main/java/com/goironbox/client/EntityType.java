package com.goironbox.client;

/**
 * Entity types.
 * 
 * @since   2.0
 */
public enum EntityType {

    /**
     * Email address.
     */
    EMAIL_ADDRESS("0"),

    /**
     * Name identifier.
     */
    NAME_IDENTIFIER("1"),

    /**
     * Entity ID.
     */
    ENTITY_ID("2");
    
    private final String restString;

    private EntityType(String s) {
        restString = s;
    }

    String getRESTString() {
        return restString;
    }

}
