package com.goironbox.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

class Crypt {

    private static final Logger logger = Logger.getInstance();

    private final static String CRYPT_ALGORITHM = "AES";
    private final static String CRYPT_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static void encryptFile(File inputFile, File outputFile, String keyBase64, String ivBase64) throws Exception {
        FileInputStream is = null;
        CipherOutputStream os = null;
        try {
            byte[] key = DatatypeConverter.parseBase64Binary(keyBase64);
            byte[] iv = DatatypeConverter.parseBase64Binary(ivBase64);
            Cipher cipher = Cipher.getInstance(CRYPT_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM), new IvParameterSpec(iv));
            is = new FileInputStream(inputFile);
            os = new CipherOutputStream(new FileOutputStream(outputFile), cipher);
            Helper.copyStream(is, os, 1024);
        }
        catch (Exception e) {
            String msg = ("Unable to encrypt file: " + inputFile.getAbsolutePath());
            logger.error(msg);
            throw new Exception(msg, e);
        }
        finally {
            Helper.closeStream(is);
            Helper.closeStream(os);
        }
    }

    public static void decryptFile(File inputFile, File outputFile, String keyBase64, String ivBase64) throws Exception {
        FileInputStream is = null;
        CipherOutputStream os = null;
        try {
            byte[] key = DatatypeConverter.parseBase64Binary(keyBase64);
            byte[] iv = DatatypeConverter.parseBase64Binary(ivBase64);
            Cipher cipher = Cipher.getInstance(CRYPT_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CRYPT_ALGORITHM), new IvParameterSpec(iv));
            is = new FileInputStream(inputFile);
            os = new CipherOutputStream(new FileOutputStream(outputFile), cipher);
            Helper.copyStream(is, os, 1024);
        }
        catch (Exception e) {
            String msg = ("Unable to decrypt file: " + inputFile.getAbsolutePath());
            logger.error(msg);
            throw new Exception(msg, e);
        }
        finally {
            Helper.closeStream(is);
            Helper.closeStream(os);
        }
    }

}
