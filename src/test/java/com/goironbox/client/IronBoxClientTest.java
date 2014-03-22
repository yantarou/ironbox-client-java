package com.goironbox.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IronBoxClientTest {

    // Property files containing API access parameters.
    List<File> API_PROPERTY_FILES = new ArrayList<File>() {{
        add(new File("prod-loadbalancer-api.properties"));
        add(new File("prod-server1-api.properties"));
        add(new File("prod-server2-api.properties"));
        add(new File("staging-api.properties"));
    }};

    private final ApiVersion API_VERSION = ApiVersion.LATEST;
    private final boolean DELETE_TMP_FILES = true;

    @Test
    public void test() throws Exception {
        for (File f : API_PROPERTY_FILES) {
            if (!f.exists()) {
                logError("File not found: " + f.getAbsolutePath());
                continue;
            }
            
            // Load PROD API properties.
            Properties prodProps = new Properties();
            prodProps.load(new FileReader(f));
            String apiBaseUrl = prodProps.getProperty("apiBaseURL");
            String email = prodProps.getProperty("email");
            String password = prodProps.getProperty("password");
            String context = prodProps.getProperty("context");
            boolean verifySSLCert = Boolean.parseBoolean(prodProps.getProperty("verifySSLCert"));
            logInfo(
                String.format(
                    "Testing: apiBaseUrl='%s', email='%s', password='%s', context='%s', verifySSLCert='%s'",
                    apiBaseUrl, email, password, context, verifySSLCert
                )
            );
            testAPIServer(apiBaseUrl, email, password, context, verifySSLCert);
        }
    }

    // ---------------------------------------------------
    // Combined Unit tests
    // ---------------------------------------------------
    private void testAPIServer(String apiBaseURL, String email, String password, String context, boolean verifySSLCert) throws Exception {
        logInfo("");
        logInfo("");
        logInfo("*********************************************************");
        logInfo("* Test for " + apiBaseURL);
        logInfo("*********************************************************");

        logInfo("Starting test for " + apiBaseURL);

        // -----------------------------------------------------
        // Create an instance of the IronBox REST class
        // -----------------------------------------------------
        IronBoxClient ibc = new IronBoxClient(
            email, password, EntityType.EMAIL_ADDRESS,
            API_VERSION, ContentFormat.JSON,
            true, verifySSLCert
        );

        // -----------------------------------------------------
        // Override the API server endpoint URL if needed,
        // point to a test server for example
        // -----------------------------------------------------
        ibc.setAPIBaseURL(apiBaseURL);

        // -----------------------------------------------------
        // Create a container
        // -----------------------------------------------------
        SFTContainerConfig cc = testCreateSFTContainer(ibc, context);
        Long containerID = cc.getContainerID();

        // -----------------------------------------------------
        // Upload and download test
        // Small size file, 32 bytes
        // -----------------------------------------------------
        File f = createSmallSizeFile();
        uploadAndDownloadTest(ibc, containerID, f);

        // -----------------------------------------------------
        // Upload and download test
        // Random sized file (from 1 MB to say 3 MB)
        // -----------------------------------------------------
        f = createRandomSizedFile();
        uploadAndDownloadTest(ibc, containerID, f);

        // -----------------------------------------------------
        // Upload and download test
        // Random sized file that is exactly 
        // a multiple of 1024 bytes. Doesn't matter what size, just has 
        // to be a multiple of 1024.
        // -----------------------------------------------------
        f = createFileHasMultipleOf1024Bytes();
        uploadAndDownloadTest(ibc, containerID, f);

        // -----------------------------------------------------
        // Upload and download test
        // Random sized file that is large (> 10 MB), max up to 20 MB.
        // -----------------------------------------------------
        f = createLargeSizeFile();
        uploadAndDownloadTest(ibc, containerID, f);

        // -----------------------------------------------------
        // Get the list of containers in the context and verify
        // that our container ID is returned in that list 
        // -----------------------------------------------------
        testContainerListHasContainer(ibc, context, containerID);

        // -----------------------------------------------------
        // Delete the container
        // -----------------------------------------------------
        testDeleteContainer(ibc, containerID);

        // -----------------------------------------------------
        // Get the list of containers in the context and verify
        // that our container ID is no longer returned in that list 
        // -----------------------------------------------------
        testContainerListDoesNotHaveContainer(ibc, context, containerID);

        // -----------------------------------------------------
        // Get basic context settings
        // -----------------------------------------------------
        testContextSetting(ibc, context);

        // -----------------------------------------------------
        // Done
        // -----------------------------------------------------
        logInfo("Done");
    }    

    
    // -------------------------------------------------------------
    // Unit Test: Create container
    // -------------------------------------------------------------
    private SFTContainerConfig testCreateSFTContainer(IronBoxClient ibc, String context) throws Exception {
        // Create a context container config 
        String containerName = "New container name";
        String containerDescription = "Description of the new container (optional)";

        // Create the test container
        SFTContainerConfig cc = ibc.createEntitySFTContainer(context, containerName, containerDescription);
        Assert.assertNotNull("Unable to create container", cc);

        logInfo("Container creation = PASSED");
        return cc;
    }
    
    // -------------------------------------------------------------
    // Unit Test: Delete container
    // -------------------------------------------------------------
    private void testDeleteContainer(IronBoxClient ibc, Long containerID) throws Exception {
        // Attempt to delete the container
        Assert.assertTrue("Unable to remove container", ibc.removeEntityContainer(containerID));

        // Create a random int-64 and try to delete it, it should fail
        Long randomContainerID = new Random().nextLong();
        Assert.assertFalse("Was able to delete a random container ID", ibc.removeEntityContainer(randomContainerID));

        logInfo("Container delete = PASSED");
    }
    
    // -------------------------------------------------------------
    // Unit Test: Upload file
    // -------------------------------------------------------------
    private boolean testUploadFile(IronBoxClient ibc, Long containerID, File f, String blobName) throws Exception {
        boolean success = ibc.uploadFileToContainer(containerID, f, blobName);
        Assert.assertTrue("Unable to upload file", success);
        if (success) {
            logInfo("File uploading = PASSED");
        }
        return success;
    }

    // -------------------------------------------------------------
    // Unit Test: Download file
    // -------------------------------------------------------------
    private File testDownloadFile(IronBoxClient ibc, Long containerID, String blobID) throws Exception {
        File f = File.createTempFile("tmp", "tmp");
        if (DELETE_TMP_FILES) {
            f.deleteOnExit();
        }

        boolean success = ibc.downloadBlobFromContainer(containerID, blobID, f);
        Assert.assertTrue("Unable to download file", success);
        return f;
    }

    // -------------------------------------------------------------
    // Unit Test: Get blob list
    // -------------------------------------------------------------
    private String testBlobListHasBlob(IronBoxClient ibc, Long containerID, String blobName) throws Exception {
        List<BlobInfo> biList = ibc.getContainerBlobInfoListByState(containerID, BlobState.READY);
        Assert.assertFalse("Unable to get blob info list", biList.isEmpty());
        
        for (BlobInfo bi : biList) {
            if (bi.getBlobName().equals(blobName)) {
                logInfo("Blob found = PASSED");
                return bi.getBlobID();
            }
        }

        Assert.fail("Unable to find uploaded blob");
        return null;
    }    
    
    // -------------------------------------------------------------
    // Unit Test: Get container list
    // -------------------------------------------------------------
    private void testContainerListHasContainer(IronBoxClient ibc, String context, Long containerID) throws Exception {
        List<ContainerInfo> ciList = ibc.getContainerInfoListByContext(context, ContainerType.DEFAULT);
        Assert.assertFalse("Unable to get container list", ciList.isEmpty());
        
        boolean containerFound = false;
        for (ContainerInfo ci : ciList) {
            if (ci.getContainerID().equals(containerID)) {
                containerFound = true;
                break;
            }
        }
        Assert.assertTrue(containerFound);
        if (containerFound) {
            logInfo("Container found = PASSED");
        }
    }

    private void testContainerListDoesNotHaveContainer(IronBoxClient ibc, String context, Long containerID) throws Exception {
        List<ContainerInfo> ciList = ibc.getContainerInfoListByContext(context, ContainerType.DEFAULT);
        Assert.assertFalse("Unable to get container list", ciList.isEmpty());

        boolean containerFound = false;
        for (ContainerInfo ci : ciList) {
            if (ci.getContainerID().equals(containerID)) {
                containerFound = true;
                logError("Container found which shouldn't");
                break;
            }
        }
        Assert.assertFalse(containerFound);
        if (!containerFound) {
            logInfo("Container not found = PASSED");
        }
    }
    
    // -------------------------------------------------------------
    // Unit Test: Context settings
    // -------------------------------------------------------------
    private void testContextSetting(IronBoxClient ibc, String context) throws Exception {
        // Attempt to get the basic context settings
        String companyName = ibc.getContextSetting(context, ContextSetting.COMPANY_NAME);
        Assert.assertNotNull("Unable to get company name context setting", companyName);
        Assert.assertFalse("Unable to get company name context setting", companyName.isEmpty());
    
        String companyLogoUrl = ibc.getContextSetting(context, ContextSetting.COMPANY_LOGO_URL);
        Assert.assertNotNull("Unable to get company logo URL context setting", companyLogoUrl);
        Assert.assertFalse("Unable to get company logo URL context setting", companyLogoUrl.isEmpty());

        // Done, passed all tests 
        logInfo("Context settings = PASSED");
    }

    // -------------------------------------------------------------
    // Console logger
    // -------------------------------------------------------------
    private void logInfo(String msg) {
        System.out.println(msg);
    }

    private void logError(String msg) {
        System.err.println(msg);
    }
    
    // -------------------------------------------------------------
    // Creating files
    // -------------------------------------------------------------
   private File createSmallSizeFile() throws IOException {
        // Small size file, 32 bytes
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        File f = File.createTempFile("tmp", "tmp");
        if (DELETE_TMP_FILES) {
            f.deleteOnExit();
        }
        FileUtils.writeByteArrayToFile(f, bytes);
        logInfo(String.format("Created tmp file with size %d bytes at: %s", f.length(), f.getAbsolutePath()));
        return f;
    }
    
    private File createRandomSizedFile() throws IOException {
        // Random sized file (from 1 MB to say 3 MB)
        int min = 1024 * 1024 * 1;
        int max = 1024 * 1024 * 3;
        int size = new Random().nextInt(max - min + 1) + min;
        byte[] bytes = new byte[size];
        new Random().nextBytes(bytes);
        File f = File.createTempFile("tmp", "tmp");
        if (DELETE_TMP_FILES) {
            f.deleteOnExit();
        }
        FileUtils.writeByteArrayToFile(f, bytes);
        logInfo(String.format("Created tmp file with size %d bytes at: %s", f.length(), f.getAbsolutePath()));
        return f;
    }

    private File createFileHasMultipleOf1024Bytes() throws IOException {
        // Random sized file that is exactly a multiple of 1024 bytes. Doesn't matter 
        // what size, just has to be a multiple of 1024.
        int unit = new Random().nextInt(50 - 1 + 1) + 1;
        byte[] bytes = new byte[1024 * unit];
        new Random().nextBytes(bytes);
        File f = File.createTempFile("tmp", "tmp");
        if (DELETE_TMP_FILES) {
            f.deleteOnExit();
        }
        FileUtils.writeByteArrayToFile(f, bytes);
        logInfo(String.format("Created tmp file with size %d bytes (%d * %d) at: %s", f.length(), unit, 1024, f.getAbsolutePath()));
        return f;
    }

    private File createLargeSizeFile() throws IOException {
        // Random sized file that is large (> 10 MB, max up to 20 MB).
        int min = 1024 * 1024 * 10;
        int max = 1024 * 1024 * 20;
        int size = new Random().nextInt(max - min + 1) + min;
        byte[] bytes = new byte[size];
        new Random().nextBytes(bytes);
        File f = File.createTempFile("tmp", "tmp");
        if (DELETE_TMP_FILES) {
            f.deleteOnExit();
        }
        FileUtils.writeByteArrayToFile(f, bytes);
        logInfo(String.format("Created tmp file with size %d bytes at: %s", f.length(), f.getAbsolutePath()));
        return f;
    }

    // -------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------
    private void uploadAndDownloadTest(IronBoxClient ibc, Long containerID, File f) throws Exception {
        // Upload the file
        String blobName = String.format("java_unit_test_%s", f.getName());
        testUploadFile(ibc, containerID, f, blobName);

        // Verify that the file is in the listing of files that
        // are ready to be downloaded
        String blobID = testBlobListHasBlob(ibc, containerID, blobName);

        // Download the file
        File downloadedFile = testDownloadFile(ibc, containerID, blobID);

        // Verify that the downloaded file and the local file
        // cryptographically are the same by comparing their
        // md5sum values
        String uploadedFileMD5 = DigestUtils.md5Hex(new FileInputStream(f));
        String downloadedFileMD5 = DigestUtils.md5Hex(new FileInputStream(downloadedFile));
        boolean equals = uploadedFileMD5.equals(downloadedFileMD5);
        Assert.assertTrue("md5sum does not match", equals);
        if (equals) {
            logInfo("md5sum matches: " + uploadedFileMD5);
        }
    }

}
