package com.goironbox.client;

/**
 * Context settings.
 * 
 * @since   2.0
 */
public enum ContextSetting {

    /**
     * Company name.
     */
    COMPANY_NAME("CompanyName"),

    /**
     * Company logo URL.
     */
    COMPANY_LOGO_URL("CompanyLogoUrl");
    
    private final String restString;

    private ContextSetting(String s) {
        restString = s;
    }

    String getRESTString() {
        return restString;
    }

}
