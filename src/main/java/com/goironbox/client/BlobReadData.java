package com.goironbox.client;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Blob read data.
 * 
 * @since   2.0
 */
public class BlobReadData {
    
    private final String containerStorageName;
    private final String sharedAccessSignature;
    private final URI sharedAccessSignatureUri;
    private final String storageType;
    private final URI storageUri;

    private BlobReadData(String jsonString) throws URISyntaxException {
        JSONObject jo = new JSONObject(jsonString);

        containerStorageName = jo.get("ContainerStorageName").toString();
        sharedAccessSignature = jo.get("SharedAccessSignature").toString();
        sharedAccessSignatureUri = new URI(jo.get("SharedAccessSignatureUri").toString());
        storageType = jo.get("StorageType").toString();
        storageUri = new URI(jo.get("StorageUri").toString());
    }

    static BlobReadData getInstance(String jsonString) throws URISyntaxException {
        return new BlobReadData(jsonString);
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
    public URI getSharedAccessSignatureURI() {
        return sharedAccessSignatureUri;
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
        return storageUri;
    }

}
