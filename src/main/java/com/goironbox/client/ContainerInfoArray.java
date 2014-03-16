package com.goironbox.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Array of container info objects.
 * 
 * @since   2.0
 */
class ContainerInfoArray {
    
    private final List<ContainerInfo> containerInfoList = new ArrayList<>();

    private ContainerInfoArray(String jsonString) {
        JSONObject jo = new JSONObject(jsonString);

        JSONArray ja = jo.getJSONArray("ContainerInfoArray");
        for (int i = 0; i < ja.length(); i++) {
            containerInfoList.add(ContainerInfo.getInstance(ja.getJSONObject(i).toString()));
        }
    }

    static ContainerInfoArray getInstance(String jsonString) {
        return new ContainerInfoArray(jsonString);
    }
    
    /**
     * Gets a list of container info objects.
     * 
     * @return list of container info objects
     */
    public List<ContainerInfo> getContainerInfoList() {
        return containerInfoList;
    }

}
