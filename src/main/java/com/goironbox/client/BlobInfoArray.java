package com.goironbox.client;

/**
 * Array of blob info objects.
 * 
 * @since   2.0
 */
import java.util.ArrayList;
import java.util.List;

class BlobInfoArray {
    
    private final List<BlobInfo> blobInfoList = new ArrayList<>();

    private BlobInfoArray(String jsonString) {
        JSONObject jo = new JSONObject(jsonString);

        JSONArray ja = jo.getJSONArray("BlobInfoArray");
        for (int i = 0; i < ja.length(); i++) {
            blobInfoList.add(BlobInfo.getInstance(ja.getJSONObject(i).toString()));
        }
    }

    static BlobInfoArray getInstance(String jsonString) {
        return new BlobInfoArray(jsonString);
    }
    
    /**
     * Gets a list of blob info objects.
     * 
     * @return list of blob info objects
     */
    public List<BlobInfo> getBlobInfoList() {
        return blobInfoList;
    }

}
