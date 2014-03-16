package com.goironbox.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;

class RESTHandler {

    private final Logger logger = Logger.getInstance();

    private final static String API_BASE_URL = "https://api.goironcloud.com";

    private final String entity;
    private final String entityPassword;
    private final EntityType entityType;
    private final ApiVersion apiVersion;
    private final ContentFormat contentFormat;
    private final boolean verifySSLCert;
    private URL apiUrl;
    
    public RESTHandler(
        String entity,
        String entityPassword,
        EntityType entityType,
        ApiVersion apiVersion,
        ContentFormat contentFormat,
        boolean verifySSLCert
    ) throws MalformedURLException {
        this.entity = entity;
        this.entityPassword = entityPassword;
        this.entityType = entityType;
        this.apiVersion = apiVersion;
        this.contentFormat = contentFormat;
        this.verifySSLCert = verifySSLCert;

        apiUrl = new URL(String.format("%s/%s/", API_BASE_URL, apiVersion));
    }

    protected void setAPIBaseURL(String apiBaseUrl) throws Exception {
        if (!apiBaseUrl.endsWith("/")) {
            apiBaseUrl += "/";
        }
        apiUrl = new URL(String.format("%s/%s/", apiBaseUrl, apiVersion.getRESTString()));
    }

    protected boolean checkInEntityContainerBlob(Long containerID, String blobIDName, Long blobSizeBytes, String blobCheckInToken) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "CheckInEntityContainerBlob");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));
            postData.put("BlobIDName", blobIDName);
            postData.put("BlobSizeBytes", Long.toString(blobSizeBytes));
            postData.put("BlobCheckInToken", blobCheckInToken);

            String result = doHttpPost(url, postData);
            return Boolean.parseBoolean(result);
        }
        catch (Exception e) {
            String msg = "Unable to check-in container blob.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected BlobCheckOutData checkOutEntityContainerBlob(Long containerID, String blobIDName) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "CheckOutEntityContainerBlob");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));
            postData.put("BlobIDName", blobIDName);

            String result = doHttpPost(url, postData);
            return BlobCheckOutData.getInstance(result);
        }
        catch (Exception e) {
            String msg = "Unable to check-out container blob.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected String createEntityContainerBlob(Long containerID, String blobName) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "CreateEntityContainerBlob");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));
            postData.put("BlobName", blobName);

            String result = doHttpPost(url, postData);
            return result.replace("\"", "");
        }
        catch (Exception e) {
            String msg = "Unable to create blob in container.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected SFTContainerConfig createEntitySFTContainer(String context, String name, String description) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "CreateEntitySFTContainer");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("Context", context);
            postData.put("Name", name);
            postData.put("Description", description);

            String result = doHttpPost(url, postData);
            return SFTContainerConfig.getInstance(result);
        }
        catch (Exception e) {
            String msg = "Unable to create entity SFT container.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected List<BlobInfo> getContainerBlobInfoListByState(Long containerID, BlobState blobState) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "GetContainerBlobInfoListByState");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));
            postData.put("BlobState", blobState.getRESTString());

            String result = doHttpPost(url, postData);
            return BlobInfoArray.getInstance(result).getBlobInfoList();
        }
        catch (Exception e) {
            String msg = "Unable to get container blob info list by state.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected Long getContainerIDFromFriendlyID(String containerFriendlyID) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "GetContainerIDFromFriendlyID");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerFriendlyID", containerFriendlyID);

            String result = doHttpPost(url, postData);
            return Long.parseLong(result);
        }
        catch (Exception e) {
            String msg = "Unable to get container ID from friendly ID.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected List<Long> getContainerIDsFromName(String containerName) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "GetContainerIDsFromName");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerName", containerName);

            String result = doHttpPost(url, postData);

            // Need to remove quotation marks from result string.
            result = result.replace("\"", "");

            List<Long> containerIDs = new ArrayList<>();
            for (String token : result.split(",")) {
                containerIDs.add(Long.parseLong(token));
            }
            return containerIDs;
        }
        catch (Exception e) {
            String msg = "Unable to get container IDs from name.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected List<ContainerInfo> getContainerInfoListByContext(String context, ContainerType containerType) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "GetContainerInfoListByContext");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("Context", context);
            postData.put("ContainerType", containerType.getRESTString());

            String result = doHttpPost(url, postData);
            return ContainerInfoArray.getInstance(result).getContainerInfoList();
        }
        catch (Exception e) {
            String msg = "Unable to get container info list by context.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected ContainerKeyData getContainerKeyData(Long containerID) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "ContainerKeyData");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));

            String result = doHttpPost(url, postData);
            return new ContainerKeyData(result);
        }
        catch (Exception e) {
            String msg = "Unable to retrieve container key data.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected String getContextSetting(String context, ContextSetting contextSetting) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "GetContextSetting");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("Context", context);
            postData.put("ContextSetting", contextSetting.getRESTString());

            String result = doHttpPost(url, postData);

            // Need to remove quotation marks from result string.
            result = result.replace("\"", "");

            return result;
        }
        catch (Exception e) {
            String msg = "Unable to get context setting.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected boolean doHttpGet(URL url, File destFile) throws Exception {
        InputStream is = null;
        OutputStream os = null;

        try {
            if (!destFile.createNewFile()) {
                String msg = "File already exists: " + destFile;
                logger.error(msg);
                throw new Exception(msg);
            }
            os = new FileOutputStream(destFile);
            HttpsURLConnection con = getConnection(url);
            con.setRequestMethod("GET");
            int rc = con.getResponseCode();
            if (HttpsURLConnection.HTTP_OK == rc) {
                is = con.getInputStream();
                long contentLength = con.getContentLengthLong();
                long bytesRead = 0;
                
                
                int chunkSize = 1024;
                byte[] temp = new byte[chunkSize];
                int chunkBytesRead = 0;
                while (-1 != chunkBytesRead) {
                    chunkBytesRead = is.read(temp, 0, chunkSize);
                    bytesRead += chunkBytesRead;
                    if (-1 != chunkBytesRead) {
                        os.write(temp, 0 , chunkBytesRead);
                        os.flush();
                    }

                    // Show progress if needed.
                    int done = (int)((50 * bytesRead) / contentLength);
                    logger.progress(
                        "\r[%s%s] %d byte(s) received",
                        new String(new char[done]).replace("\0", "="),
                        new String(new char[50 - done]).replace("\0", " "),
                        bytesRead
                    );
                }
                logger.progressDone();
            }
            else {
                String msg = String.format("HTTP GET request failed! ERROR: %d (%s)", rc, con.getResponseMessage());
                logger.error(msg);
                throw new Exception(msg);
            }
        }
        catch (Exception e) {
            String msg = "HTTP GET request failed.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
        finally {
            Helper.closeStream(is);
            Helper.closeStream(os);
        }
        return true;
    }
    
    private String doHttpPost(URL url, Map<String, String> postData) throws Exception {
        OutputStream os = null;
        BufferedWriter writer = null;
        try {
            String postQuery = getHttpPostQuery(postData);

            HttpsURLConnection con = getConnection(url);
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", Integer.toString(postQuery.length()));
            con.setRequestProperty("Accept", contentFormat.getRESTString());

            os = con.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postQuery);
            writer.flush();

            int rc = con.getResponseCode();
            if (HttpsURLConnection.HTTP_OK == rc) {
                InputStreamReader in = null;
                BufferedReader br = null;
                try {
                    String contentEncoding = con.getContentEncoding();
                    if (null == contentEncoding) {
                        // Assume UTF-8 if encoding is not set explicitely.
                        contentEncoding = "UTF-8";
                    }
                    in = new InputStreamReader(con.getInputStream(), contentEncoding);
                    br = new BufferedReader(in);

                    StringBuilder result = new StringBuilder();
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        result.append(strLine);
                    }
                    return result.toString();
                }
                finally {
                    Helper.closeStream(br);
                    Helper.closeStream(in);
                }
            }
            else {
                String msg = String.format("HTTP POST request failed! ERROR: %d (%s)", rc, con.getResponseMessage());
                logger.error(msg);
                throw new Exception(msg);
            }
        }
        catch (Exception e) {
            String msg = "HTTP POST request failed.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
        finally {
            Helper.closeStream(writer);
            Helper.closeStream(os);
        }
    }

    private boolean doHttpPut(URL url, Map<String, String> requestProperties, byte[] buf) throws Exception {
        OutputStream os = null;
        try {
            HttpsURLConnection con = getConnection(url);
            con.setDoOutput(true);
            con.setRequestMethod("PUT");
            for (Entry<String, String> e : requestProperties.entrySet()) {
                con.setRequestProperty(e.getKey(), e.getValue());
            }

            os = con.getOutputStream();
            os.write(buf);
            os.flush();

            int rc = con.getResponseCode();
            if (HttpsURLConnection.HTTP_CREATED == rc) {
                return true;
            }
            else {
                logger.error("HTTP PUT request failed. ERROR: %d (%s)", rc, con.getResponseMessage());
                return false;
            }
        }
        catch (Exception e) {
            String msg = "HTTP PUT request failed.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
        finally {
            Helper.closeStream(os);
        }
    }

    private String getHttpPostQuery(Map<String, String> postData) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (Entry<String, String> e : postData.entrySet()) {
            if (first) {
                first = false;
            }
            else {
                result.append("&");
            }

            result.append(URLEncoder.encode(e.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(e.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    protected boolean ping() throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "Ping");
            HttpsURLConnection con = getConnection(url);
            int rc = con.getResponseCode();
            if (HttpsURLConnection.HTTP_OK == rc) {
                return true;
            }
            else {
                String msg = "IronBox API server is not accessible from this network location.";
                logger.error(msg);
                return false;
            }
        }
        catch (Exception e) {
            String msg = "IronBox API server is not accessible from this network location.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected BlobReadData readEntityContainerBlob(Long containerID, String blobIDName) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "ReadEntityContainerBlob");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));
            postData.put("BlobIDName", blobIDName);

            String result = doHttpPost(url, postData);
            return BlobReadData.getInstance(result);
        }
        catch (Exception e) {
            String msg = "Unable to read entitiy container blob.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected boolean removeEntityContainer(Long containerID) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "RemoveEntityContainer");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));

            String result = doHttpPost(url, postData);
            return Boolean.parseBoolean(result);
        }
        catch (Exception e) {
            String msg = "Unable to remove entitiy container.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected boolean removeEntityContainerBlob(Long containerID, String blobIDName) throws Exception {
        try {
            URL url = new URL(apiUrl.toString() + "RemoveEntityContainer");

            Map<String, String> postData = new HashMap<>();
            postData.put("Entity", entity);
            postData.put("EntityType", entityType.getRESTString());
            postData.put("EntityPassword", entityPassword);
            postData.put("ContainerID", Long.toString(containerID));
            postData.put("BlobIDName", blobIDName);

            String result = doHttpPost(url, postData);
            return Boolean.parseBoolean(result);
        }
        catch (Exception e) {
            String msg = "Unable to remove entitiy container blob.";
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    protected boolean uploadBlobWithSharedAccessSignatureUri(File localFile, URI sasURI) throws Exception {
        if (!localFile.exists() || !localFile.isFile()) {
            throw new FileNotFoundException(String.format("File not found: '%s'", localFile.getAbsolutePath()));
        }

	// Cloud storage only allows blocks of max 4MB, and max 50k blocks
	// so 200 GB max per file
	int blockSizeMB = 4;
	int blockSizeBytes = blockSizeMB * 1024 * 1024;
	logger.info("File size: %d", localFile.length());
	logger.info("Starting send in %dMB increments", blockSizeMB);

	// Send headers
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("content-type", "application/octet-stream");
        requestProperties.put("x-ms-blob-type", "BlockBlob");
        requestProperties.put("x-ms-version", "2012-02-12");

	// Open handle to encrypted file and send it in blocks
	String sasURIBlockPrefix = sasURI.toString() + "&comp=block&blockid=";
	List<String> blockIDStrings = new ArrayList<>();

        long numBytesSent = 0;
        int blockCount = 0;

        InputStream in = null;
        try {
            in = new FileInputStream(localFile);
            byte[] buf = new byte[blockSizeBytes];
            int bytesRead;
            while ((bytesRead = in.read(buf, 0, buf.length)) != -1) {
                // Block IDs all have to be the same length, which was NOT documented by MSFT.
                String blockIDString = String.format("block%08d", blockCount);
                String blockSASUrl = sasURIBlockPrefix + DatatypeConverter.printBase64Binary(blockIDString.getBytes("UTF-8"));

                if (bytesRead != blockSizeBytes) {
                    buf = Arrays.copyOf(buf, bytesRead);
                }
                // Create a blob block
                if (!doHttpPut(new URL(blockSASUrl), requestProperties, buf)) {
                    logger.error("Failed to upload blob block! (block=%d, size=%d)", blockCount, buf.length);
                    return false;
                }

                // Block was successfuly sent, record its ID
                blockIDStrings.add(blockIDString);
                numBytesSent += bytesRead;
                blockCount++;

                // Show progress if needed.
                int done = (int)((50 * numBytesSent) / localFile.length());
                logger.progress(
                    "\r[%s%s] %d byte(s) sent",
                    new String(new char[done]).replace("\0", "="),
                    new String(new char[50 - done]).replace("\0", " "),
                    numBytesSent
                );
            }
            logger.progressDone();
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            Helper.closeStream(in);
        }

	// Done sending blocks, so commit the blocks into a single one
	// do the final re-assembly on the storage server side
	String commitBlockSASUrl = sasURI.toString() + "&comp=blockList";
        Map<String, String> commitRequestProperties = new HashMap<>();
        commitRequestProperties.put("content-type", "text/xml");
        commitRequestProperties.put("x-ms-version", "2012-02-12");

	//String blockListBody = "";
        StringBuilder blockListBody = new StringBuilder();
        for (String s : blockIDStrings) {
            String encodedBlockID = DatatypeConverter.printBase64Binary(s.getBytes("UTF-8"));
	    //Indicate blocks to commit per 2012-02-12 version PUT block list specifications
	    blockListBody.append(String.format("<Latest>%s</Latest>", encodedBlockID));
        }
	String commitBody = String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?><BlockList>%s</BlockList>", blockListBody.toString());
        return doHttpPut(new URL(commitBlockSASUrl), commitRequestProperties, commitBody.getBytes("UTF-8"));
    }

    private HttpsURLConnection getConnection(URL url) throws Exception {
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
        if (!verifySSLCert) {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] {new SSLInvalidCertificateTrustManager()}, new SecureRandom());
            con.setSSLSocketFactory(sc.getSocketFactory());
            con.setHostnameVerifier(new SSLInvalidHostnameVerifier());
        }
        return con;
    }

}
