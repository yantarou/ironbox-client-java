package com.goironbox.client;

/**
 * Container info.
 * 
 * @since   2.0
 */
public class ContainerInfo {

    private final Long containerID;
    private final String containerName;
    
    private ContainerInfo(String jsonString) {
        JSONObject jo = new JSONObject(jsonString);
       
        containerID = Long.parseLong(jo.get("ContainerID").toString());
        containerName = jo.get("ContainerName").toString();
    }

    static ContainerInfo getInstance(String jsonString) {
        return new ContainerInfo(jsonString);
    }
    
    /**
     * Gets the container's ID.
     * 
     * @return the container's ID
     */
    public Long getContainerID() {
        return containerID;
    }

    /**
     * Gets the container's name.
     * 
     * @return the container's name
     */
    public String getContainerName() {
        return containerName;
    }

}
