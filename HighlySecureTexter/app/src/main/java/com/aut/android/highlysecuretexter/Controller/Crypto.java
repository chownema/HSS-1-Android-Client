package com.aut.android.highlysecuretexter.Controller;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Created by Adam on 30/09/16.
 */

public class Crypto {

    public final static byte[] IV = {-84, 40, -10, -53, -80, 90, -57, 125, -84, 40, -10, -53, -80, 90, -57, 125};

    public static byte[] encryptInitialConnection(Client client, PublicKey pkaPublicKey) {
        try {
            SecretKey ephemeral = client.getEphemeralKey();

            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec initVector = new IvParameterSpec(IV);
            // initialize cipher for encryption
            aesCipher.init(Cipher.ENCRYPT_MODE, ephemeral, initVector);

            //Encrypt nonce with pub key of pka (added security)
            byte[] nonceBytes = encryptRSA(pkaPublicKey, client.getMobile().getBytes());

            //Package data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(client.getMobile().getBytes());
            baos.write("---".getBytes());
            baos.write(Base64.encode(nonceBytes, Base64.NO_WRAP)); // encrypted with private RSA
            baos.write("---".getBytes());
            baos.write(Base64.encode(client.getPublicKey().getEncoded(), Base64.NO_WRAP));

            // Encrypt and return
            byte[] cipherBytes = aesCipher.doFinal(baos.toByteArray());
            return cipherBytes;

        } catch (Exception ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }

    public static byte[] encryptRSA(Key key, byte[] data) {

        byte[] encrypted = null;

        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = rsaCipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (InvalidKeyException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (BadPaddingException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        }

        return encrypted;
    }

    public static byte[] decryptRSA(Key key, byte[] data) {

        byte[] decrypted = null;

        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = rsaCipher.doFinal(data);
        } catch (NoSuchAlgorithmException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (InvalidKeyException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (BadPaddingException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e("RSA Encrypt Error:", e.getMessage());
        }

        return decrypted;
    }
}
