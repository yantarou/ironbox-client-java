package com.goironbox.client;

/**
 * Blob states.
 * 
 * @since   2.0
 */
public enum BlobState {

    /**
     * Blob created.
     */
    BLOB_CREATED("0"),
    
    /**
     * Entity is uploading.
     */
    ENTITY_IS_UPLOADING("1"),
    
    /**
     * Ready.
     */
    READY("2"),
    
    /**
     * Checked-out.
     */
    CHECKED_OUT("3"),
    
    /**
     * Entity is modifying.
     */
    ENTITY_IS_MODIFYING("4"),
    
    /**
     * None.
     */
    NONE("5");
    
    private final String restString;

    private BlobState(String s) {
        restString = s;
    }

    String getRESTString() {
        return restString;
    }

}
