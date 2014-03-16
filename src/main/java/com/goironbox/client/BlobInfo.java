package com.goironbox.client;

/**
 * Blob info.
 * 
 * @since   2.0
 */
public class BlobInfo {
    
    private final String blobID;
    private final String blobName;

    private BlobInfo(String jsonString) {
        JSONObject jo = new JSONObject(jsonString);

        blobID = jo.get("BlobID").toString();
        blobName = jo.get("BlobName").toString();
    }

    static BlobInfo getInstance(String jsonString) {
        return new BlobInfo(jsonString);
    }
    
    /**
     * Gets the blob's ID.
     * 
     * @return the blob's ID
     */
    public String getBlobID() {
        return blobID;
    }

    /**
     * Gets the blob's name.
     * 
     * @return the blob's name
     */
    public String getBlobName() {
        return blobName;
    }

}
