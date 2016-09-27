package com.aut.android.highlysecuretexter.Controller;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Created by MI on 27/09/16.
 */

public class ClientHelper {

    private String phoneNum;
    private String oneTimeKey;
    private SecretKey ephemeralKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey pkaPubKey;
    private final byte[] salt = {-84, 40, -10, -53, -80, 90, -57, 125};

    public ClientHelper(String phoneNum, String oneTimeKey) {
        this.phoneNum = phoneNum;
        this.oneTimeKey = oneTimeKey;
    }

    public void generateEphemeral() {

        SecretKey key = null;

        try {
            char[] passwordChar = oneTimeKey.toCharArray();
            PBEKeySpec pbeSpec = new PBEKeySpec(passwordChar, salt, 1000);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            key = keyFactory.generateSecret(pbeSpec);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (InvalidKeySpecException ex) {
            ex.printStackTrace();
        }

        ephemeralKey = key;
    }

    public void generateKeys() {

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();

            System.out.println("PubKey: " + publicKey);

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }


    public byte[] encryptDetails() {

        try {
            // Prep cipher
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
            pbeCipher.init(ENCRYPT_MODE, ephemeralKey, new PBEParameterSpec(salt, 1000));

            // Encrypt nonce with pub key of pka (added security)
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(ENCRYPT_MODE, pkaPubKey);
            byte[] nonceBytes = rsaCipher.doFinal(phoneNum.getBytes());

            // Package data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(phoneNum.getBytes());
            baos.write("---".getBytes());
            baos.write(Base64.encode(nonceBytes, Base64.DEFAULT)); // encrypted with private RSA
            baos.write("---".getBytes());
            baos.write(Base64.encode(publicKey.getEncoded(), Base64.DEFAULT));

            // Encrypt and return
            byte[] cipherBytes = pbeCipher.doFinal(baos.toByteArray());
            return cipherBytes;

        } catch (NoSuchAlgorithmException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (IllegalBlockSizeException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (BadPaddingException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (InvalidAlgorithmParameterException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (IOException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        }

        return null;
    }
}

