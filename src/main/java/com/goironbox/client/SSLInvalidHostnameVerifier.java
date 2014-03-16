package com.goironbox.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

class SSLInvalidHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }
    
}
