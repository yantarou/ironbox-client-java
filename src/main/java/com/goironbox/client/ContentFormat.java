package com.goironbox.client;

/**
 * Content formats.
 * 
 * @since   2.0
 */
public enum ContentFormat {

    /**
     * JSON.
     */
    JSON("application/json");
    
    private final String restString;

    private ContentFormat(String s) {
        restString = s;
    }

    String getRESTString() {
        return restString;
    }

}
