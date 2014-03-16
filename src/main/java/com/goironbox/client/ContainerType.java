package com.goironbox.client;

/**
 * Container types.
 * 
 * @since   2.0
 */
public enum ContainerType {

    /**
     * Default.
     */
    DEFAULT("5");
    
    private final String restString;

    private ContainerType(String s) {
        restString = s;
    }

    String getRESTString() {
        return restString;
    }

}
