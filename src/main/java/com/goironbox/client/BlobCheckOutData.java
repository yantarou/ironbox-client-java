package com.goironbox.client;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Blob check-out data.
 * 
 * @since   2.0
 */
public class BlobCheckOutData {
    
    private final String checkInToken;
    private final String containerStorageName;
    private final String sharedAccessSignature;
    private final URI sharedAccessSignatureURI;
    private final String storageType;
    private final URI storageURI;

    private BlobCheckOutData(String jsonString) throws URISyntaxException {
        JSONObject jo = new JSONObject(jsonString);

        checkInToken = jo.get("CheckInToken").toString();
        containerStorageName = jo.get("ContainerStorageName").toString();
        sharedAccessSignature = jo.get("SharedAccessSignature").toString();
        sharedAccessSignatureURI = new URI(jo.get("SharedAccessSignatureUri").toString());
        storageType = jo.get("StorageType").toString();
        storageURI = new URI(jo.get("StorageUri").toString());
    }
    
    static BlobCheckOutData getInstance(String jsonString) throws URISyntaxException {
        return new BlobCheckOutData(jsonString);
    }

    /**
     * Gets the blob's check-in token.
     * 
     * @return the blob's check-in token
     */
    public String getCheckInToken() {
        return checkInToken;
    }

    /**
     * Gets the container storage name.
     * 
     * @return the container storage name
     */
    public String getContainerStorageName() {
        return containerStorageName;
    }

    /**
     * Gets the shared access signature.
     * 
     * @return the shared access signature
     */
    public String getSharedAccessSignature() {
        return sharedAccessSignature;
    }

    /**
     * Gets the shared access signature URI.
     * 
     * @return the shared access signature URI
     */
    public URI getSharedAccessSignatureURI() throws URISyntaxException {
        return sharedAccessSignatureURI;
    }

    /**
     * Gets the storage type.
     * 
     * @return the storage type
     */
    public String getStorageType() {
        return storageType;
    }

    /**
     * Gets the storage URI.
     * 
     * @return the storage URI
     */
    public URI getStorageURI() {
        return storageURI;
    }

}
