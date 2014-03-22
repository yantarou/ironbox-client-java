package com.goironbox.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;

/**
 * IronBox REST API client.
 * 
 * @since   2.0
 */
public class IronBoxClient {

    private Logger logger;
    private RESTHandler rh;

    /**
     * 
     * @param entity Entity user name, such as an email or entity ID.
     * @param entityPassword The entity password.
     * @param entityType The {@link com.goironbox.client.EntityType}.
     * @param apiVersion The {@link com.goironbox.client.ApiVersion} to use.
     * @param contentFormat The {@link com.goironbox.client.ContentFormat} to use.
     * @param verbose Enable verbose logging.
     * @param verifySSLCert Verify SSL certificates.
     * @throws Exception 
     */
    public IronBoxClient(
        String entity,
        String entityPassword,
        EntityType entityType,
        ApiVersion apiVersion,
        ContentFormat contentFormat,
        boolean verbose,
        boolean verifySSLCert
    ) throws Exception {
        logger = Logger.getInstance(verbose);
        rh = new RESTHandler(
            entity, entityPassword, entityType,
            apiVersion, contentFormat, verifySSLCert
        );
    }

    /**
     * 
     * @param entity Entity user name, such as an email or entity ID.
     * @param entityPassword The entity password.
     * @throws Exception 
     */
    public IronBoxClient(
        String entity,
        String entityPassword
    ) throws Exception {
        this(
            entity,
            entityPassword,
            EntityType.EMAIL_ADDRESS,
            ApiVersion.LATEST,
            ContentFormat.JSON,
            false,
            true
        );
    }

    /**
     * Sets the API base URL.
     * 
     * @param apiBaseUrl The API base URL.
     * @throws Exception 
     */
    public void setAPIBaseURL(String apiBaseUrl) throws Exception {
        rh.setAPIBaseURL(apiBaseUrl);
    }

    private boolean uploadBlobWithSharedAccessSignatureUri(File localFile, URI sasURI) throws Exception {
        return rh.uploadBlobWithSharedAccessSignatureUri(localFile, sasURI);
    }

    /**
     * Uploads a file to a container.
     * 
     * @param containerID A 64-bit integer container ID.
     * @param srcFile File to upload.
     * @param blobName Blob ID to be used.
     * @return
     * @throws Exception 
     */
    public boolean uploadFileToContainer(Long containerID, File srcFile, String blobName) throws Exception {
        if (!srcFile.exists() || !srcFile.isFile()) {
            String msg = "File not found: " + srcFile.getAbsolutePath();
            logger.error(msg);
            throw new FileNotFoundException(msg);
        }

        // Step 1:
        // Test to make sure that the API server is accessible.
        if (!ping()) {
            throw new Exception("IronBox API server is not accessible from this network location!");
        }
        logger.info("IronBox API is up, starting transfer.");

        // Step 2:
        // Get the container key data.
        ContainerKeyData ckd = getContainerKeyData(containerID);
        logger.info("Retrieved container symmetric key data.");

        // Step 3:
        // Create a container blob and check it out.
        // This doesn't actually upload the contents, just creates the entry,
        // and does a "check out" which lets IronBox know you're going to upload
        // contents soon. As part of the checkout process you'll get a check in
        // token that is your way to check the blob back in.
        String blobID = createEntityContainerBlob(containerID, blobName);
        BlobCheckOutData bcod = checkOutEntityContainerBlob(containerID, blobID);

        // Step 4:
        // Encrypt the input file.
        logger.info("Encrypting file: " + srcFile.getAbsolutePath());
        File encryptedFile = new File(srcFile.getAbsolutePath() + ".ironbox");
        IronBoxClient.encryptFile(srcFile, encryptedFile, ckd);

        // Step 5:
        // Upload the encrypted file using the shared acccess signature we got
        // at checkout.
        logger.info("Uploading encrypted file: " + encryptedFile.getAbsolutePath());
        if (!uploadBlobWithSharedAccessSignatureUri(encryptedFile, bcod.getSharedAccessSignatureURI())) {
	    logger.error("Failed to upload encrypted file.");
            return false;
        }

        // Step 6:
        // Mark the file as ready to download by checking it back in.
        if (!checkInEntityContainerBlob(containerID, blobID, srcFile.length(), bcod.getCheckInToken())) {
	    logger.error("Failed to check-in blob.");
            return false;
        }

        // Delete encrypted file.
        logger.info("Upload completed, cleaning up.");
        return encryptedFile.delete();
    }

    /**
     * 
     * @param containerID A 64-bit integer container ID.
     * @param blobID Blob ID to be downloaded.
     * @param localFile File to save to.
     * @return
     * @throws Exception 
     */
    public boolean downloadBlobFromContainer(Long containerID, String blobID, File localFile) throws Exception {
        // Step 1:
        // Test to make sure that the API server is accessible.
        if (!ping()) {
            throw new Exception("IronBox API server is not accessible from this network location!");
        }
        logger.info("IronBox API is up, starting transfer.");
        logger.info("IronBox API is up, starting download of target file %s", localFile.getAbsolutePath());

        // Step 2:
        // Get the container key data.
        ContainerKeyData ckd = getContainerKeyData(containerID);
        logger.info("Retrieved container symmetric key data.");

        // Step 3:
        // Download the blob read data, specifically we need a shared access
        // signature URI to the encrypted blob .
	BlobReadData brd = readEntityContainerBlob(containerID, blobID);
	logger.info("Retrieved blob download Shared Access Signature URI");
        File encryptedFile = new File(localFile.getAbsolutePath() + ".encrypted");
        rh.doHttpGet(brd.getSharedAccessSignatureURI().toURL(), encryptedFile);

        // Step 4:
	// Decrypt the downloaded blob
	logger.info("Decrypting encrypted blob");
        decryptFile(encryptedFile, localFile, ckd);

        // Step 5:
	// Done, clean up 
	logger.info("Done, cleaning up %s", encryptedFile.getAbsolutePath());
        return encryptedFile.delete();
    }

    /**
     * Checks-in a checked-out blob indicating that this blob is ready.
     * 
     * This method should only be called after the caller has finished modifying the checked-out blob.
     * 
     * @param containerID A 64-bit integer container ID.
     * @param blobIDName A string that denotes the blob ID.
     * @param blobSizeBytes The size of the blob being checked-in in bytes.
     * @param checkInToken The token provided from the call to CheckOutEntityContainerBlob that authorizes the caller to check the blob back in and mark it in a ready state.
     * @return true if successful, false else
     * @throws Exception 
     */
    public boolean checkInEntityContainerBlob(Long containerID, String blobIDName, Long blobSizeBytes, String checkInToken) throws Exception {
        return rh.checkInEntityContainerBlob(containerID, blobIDName, blobSizeBytes, checkInToken);
    }

    /**
     * 
     * @param containerID A 64-bit integer container ID.
     * @param blobIDName A string that denotes the blob ID.
     * @return A BlobCheckOutData object if successful.
     * @throws Exception 
     */
    public BlobCheckOutData checkOutEntityContainerBlob(Long containerID, String blobIDName) throws Exception {
        return rh.checkOutEntityContainerBlob(containerID, blobIDName);
    }

    /**
     * Returns the symmetric key material for the requested container.
     * 
     * Only the owner or members of a container may execute this request.
     * It is recommended that developers take measures to protect container key data while at rest at all times.
     * 
     * @param containerID A 64-bit integer container ID.
     * @return A ContainerKeyData object if successful.
     * @throws Exception 
     */
    public ContainerKeyData getContainerKeyData(Long containerID) throws Exception {
        return rh.getContainerKeyData(containerID);
    }

    /**
     * Create a blob entry within an IronBox container.
     * 
     * @param containerID A 64-bit integer container ID.
     * @param blobName Name of the blob to create (i.e. test.txt).
     * @return A unique blob ID that the caller can reference.
     * @throws Exception 
     */
    public String createEntityContainerBlob(Long containerID, String blobName) throws Exception {
        return rh.createEntityContainerBlob(containerID, blobName);
    }

    /**
     * Encrypts a file.
     * 
     * @param inputFile The input file.
     * @param outputFile The output file.
     * @param containerKeyData A ContainerKeyData object containing encryption key material.
     * 
     * @throws Exception 
     */
    public static void encryptFile(File inputFile, File outputFile, ContainerKeyData containerKeyData) throws Exception {
        Crypt.encryptFile(inputFile, outputFile, containerKeyData.getSessionKeyBase64(), containerKeyData.getSessionIVBase64());
    }

    /**
     * Encrypts a file.
     * 
     * @param inputFile The input file.
     * @param outputFile The output file.
     * @param containerKeyData A ContainerKeyData object containing decryption key material.
     * 
     * @throws Exception 
     */
    public static void decryptFile(File inputFile, File outputFile, ContainerKeyData containerKeyData) throws Exception {
        Crypt.decryptFile(inputFile, outputFile, containerKeyData.getSessionKeyBase64(), containerKeyData.getSessionIVBase64());
    }

    /**
     * Creates an IronBox secure file transfer (SFT) container.
     * 
     * Duplicate container names are supported.
     * 
     * @param context Context (e.g. secure.goironcloud.com or demo.goironcloud.com).
     * @param name Name of the IronBox SFT container to create (required). Duplicate names are allowed.
     * @param description Description of the container to create.
     * @return An IronBoxSFTContainerConfig object on success,
     * @throws Exception 
     */
    public SFTContainerConfig createEntitySFTContainer(String context, String name, String description) throws Exception {
        return rh.createEntitySFTContainer(context, name, description);
    }

    /**
     * Gets an array of blob info objects that match the provided state.
     * 
     * For example, if a Ready state is provided, then returns the container blob info objects of all blobs that are in the Ready state.
     * 
     * @param containerID A 64-bit integer container ID.
     * @param blobState A BlobState object that represents the blob state to query.
     * @return A list of BlobInfo objects.
     * @throws Exception 
     */
    public List<BlobInfo> getContainerBlobInfoListByState(Long containerID, BlobState blobState) throws Exception {
        return rh.getContainerBlobInfoListByState(containerID, blobState);
    }

    /**
     * Gets a list of container IDs and container names that an entity is a member of by context.
     * 
     * The entity must already be a member of the context.
     * 
     * @param context Context (e.g. secure.goironcloud.com or demo.goironcloud.com).
     * @param containerType The container type.
     * @return A list of ContainerInfo objects.
     * @throws Exception 
     */
    public List<ContainerInfo> getContainerInfoListByContext(String context, ContainerType containerType) throws Exception {
        return rh.getContainerInfoListByContext(context, containerType);
    }

    /**
     * Gets the 64-bit container ID for a given container 'friendly' ID.
     * 
     * @param containerFriendlyID The container's friendly ID.
     * @return A 64-bit integer container ID.
     * @throws Exception 
     */
    public Long getContainerIDFromFriendlyID(String containerFriendlyID) throws Exception {
        return rh.getContainerIDFromFriendlyID(containerFriendlyID);
    }

    /**
     * Gets a list of container IDs for container name.
     * Only IDs for containers that the calling entity is an owner or member of will be returned.
     *
     * @param containerName The name of the container
     * @return List of containers IDs that match the container name provided
     * @throws Exception
     */
    public List<Long> getContainerIDsFromName(String containerName) throws Exception {
        return rh.getContainerIDsFromName(containerName);
    }

    /**
     * Get context setting.
     * The requesting entity must be a member of the context in order to make this call.
     *
     * @param context Context (e.g. secure.goironcloud.com or demo.goironcloud.com).
     * @param contextSetting The context setting value to return.
     * @return String value of the requested context setting.
     * @throws Exception
     */
    public String getContextSetting(String context, ContextSetting contextSetting) throws Exception {
        return rh.getContextSetting(context, contextSetting);
    }

    /**
     * Indicates if the API service is responding.
     *
     * @return true if successful, false else
     * @throws Exception
     */
    public boolean ping() throws Exception {
        return rh.ping();
    }

    /**
     *  Retrieves the storage information required to read encrypted entity container blobs directly from storage.
     *  Returned information will include storage endpoint URL, container name and a shared access signature that grants limited temporary access to back-end storage.
     *  Callers can then use the URL specified in the SharedAccessSignatureUri response to directly read the encrypted blob from storage.
     *  Once downloaded, callers must then decrypt the encrypted blob using the information provided from the call to ContainerKeyData.
     *
     * @param containerID A 64-bit integer container ID
     * @param blobID A string that denotes the blob ID
     * @return BlobReadData object
     * @throws Exception
     */
    public BlobReadData readEntityContainerBlob(Long containerID, String blobID) throws Exception {
        return rh.readEntityContainerBlob(containerID, blobID);
    }

    /**
     * Removes an IronBox entity container.
     * Caller must be the owner of the container being removed.
     *
     * @param containerID A 64-bit integer container ID
     * @return true if successful, false else
     * @throws Exception
     */
    public boolean removeEntityContainer(Long containerID) throws Exception {
        return rh.removeEntityContainer(containerID);
    }

    /**
     * Removes a blob from an entity container.
     *
     * @param containerID A 64-bit integer container ID
     * @param blobID A string that denotes the blob ID
     * @return true if successful, false else
     * @throws Exception
     */
    public boolean removeEntityContainerBlob(Long containerID, String blobID) throws Exception {
        return rh.removeEntityContainerBlob(containerID, blobID);
    }

}
