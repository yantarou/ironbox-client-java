package com.goironbox.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Helper {

    protected static void closeStream(Closeable c) {
        if (null == c) {
            return;
        }
        try {
            c.close();
        }
        catch (IOException e) {
        }
    }

    protected static void copyStream(InputStream is, OutputStream os, int blockSize) throws IOException {
        byte[] b = new byte[blockSize];
        int i;
        while ((i = is.read(b)) != -1) {
            os.write(b, 0, i);
        }
    }
    
}
