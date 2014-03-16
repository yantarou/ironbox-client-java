package com.goironbox.client;

/**
 * Secure file transfer (SFT) container configuration.
 * 
 * @since   2.0
 */
public class SFTContainerConfig {
    
    private final Long containerID;
    private final String description;
    private final String friendlyID;
    private final String name;
    
    private SFTContainerConfig(String jsonString) {
        JSONObject jo = new JSONObject(jsonString);

        containerID = Long.parseLong(jo.get("ContainerID").toString());
        description = jo.get("Description").toString();
        friendlyID = jo.get("FriendlyID").toString();
        name = jo.get("Name").toString();
    }

    static SFTContainerConfig getInstance(String jsonString) {
        return new SFTContainerConfig(jsonString);
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
     * Gets the container's description.
     * 
     * @return the container's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the container's friendly ID.
     * 
     * @return the container's friendly ID
     */
    public String getFriendlyID() {
        return friendlyID;
    }

    /**
     * Gets the container's name.
     * 
     * @return the container's name
     */
    public String getName() {
        return name;
    }

}
