package com.goironbox.client;

/**
 * Available API versions.
 * 
 * @since   2.0
 */
public enum ApiVersion {

    /**
     * Latest version.
     */
    LATEST("latest"),

    /**
     * Version 2.
     */
    V2("v2");
    
    private final String restString;

    private ApiVersion(String s) {
        restString = s;
    }

    String getRESTString() {
        return restString;
    }

}
