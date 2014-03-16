package com.goironbox.client;

/**
 * Container's key data.
 * 
 * @since   2.0
 */
public class ContainerKeyData {
    
    private final String sessionIVBase64;
    private final String sessionKeyBase64;
    private final SymmetricKeyStrength symmetricKeyStrength;

    public ContainerKeyData(String jsonString) {
        JSONObject jo = new JSONObject(jsonString);

        sessionIVBase64 = jo.get("SessionIVBase64").toString();
        sessionKeyBase64 = jo.get("SessionKeyBase64").toString();
        symmetricKeyStrength = SymmetricKeyStrength.getFromRESTString(jo.get("SymmetricKeyStrength").toString());
    }

    static ContainerKeyData getInstance(String jsonString) {
        return new ContainerKeyData(jsonString);
    }
    
    /**
     * Gets the Base64 encoded session IV.
     * 
     * @return the Base64 encoded session IV
     */
    public String getSessionIVBase64() {
        return sessionIVBase64;
    }

    /**
     * Gets the Base64 encoded session key.
     * 
     * @return the Base64 encoded session key
     */
    public String getSessionKeyBase64() {
        return sessionKeyBase64;
    }

    /**
     * Gets the symmetric key strength.
     * 
     * @return the symmetric key strength
     */
    public SymmetricKeyStrength getSymmetricKeyStrength() {
        return symmetricKeyStrength;
    }

}
