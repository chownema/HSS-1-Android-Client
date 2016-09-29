package com.aut.android.highlysecuretexter.Controller;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import static javax.crypto.Cipher.ENCRYPT_MODE;


/**
 * Created by MI on 28/09/16.
 */

public class Utility {

    public final static byte[] salt = {-84, 40, -10, -53, -80, 90, -57, 125};
    public final static String endpoint = "http://172.28.56.205:8080/PKAServer/webresources/pka/";

    // One off Key
    public static SecretKey ephemeralKey = null;

    // Keys
    public static PublicKey pkaPubKey = null;
    public static PrivateKey privateKey = null;
    public static PublicKey publicKey = null;

    // Aes Encryption
    private static IvParameterSpec initVector;
    private static byte[] ivBytes = { 1, -2, 3, -4, 5, -6, 7, -8, 9,
            -10, 11, -12, 13, -14, 15, -16 }; // random array of 16 bytes

    // Debug variables
    public static PrivateKey privateKeyB = null;
    public static PublicKey publicKeyB = null;

    /**
     * Debugging Functions
     */

    public static String getPassword(String number)
    {
        return doPost("request/"+number);
    }

    public static void initDebugValues()
    {
        // Initialize debug values

        // Encrypted Messaging test
        // Init RSA messaging variables
        initMessagingEncryption();
        // Create 2 local key pairs for messaging
        generateDebugKeys();
        generateKeys();
    }

    public static void generateDebugKeys() {
        try {
            // Generate key pair for B
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            publicKeyB = keyPair.getPublic();
            privateKeyB = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException ex) {
            Log.e("Error", ex.toString());
        }
    }

    /**
     * Encryption Functions
     */

    public static void initMessagingEncryption()
    {
        // Initialize messaging values

        // init iv for AES cipher
        initVector = new IvParameterSpec(ivBytes);

    }

    public static void init(String pNumber, String password) {

        byte[] pkaPubKeyBytes = decodeFromBase64(doPost("pkakey"));

        // get pka key
        try {
            pkaPubKey = KeyFactory.getInstance("RSA").generatePublic
                    (new X509EncodedKeySpec(pkaPubKeyBytes));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Check PKA Key is null
        if (pkaPubKey == null) {
            throw new RuntimeException("Key Error");
        }

        // Get password and generate one time key
        generateEphemeral(password);

        // Generate Clients key pair
        generateKeys();

        // Create Encrypted encrypted data
        byte[] encryptedConnPackage = encryptConnectionData(pNumber);

        // Base64 encode encrypted package into a string
        String bytesEncoded = encodeToBase64(encryptedConnPackage);

        // Send request to join to PKA
        doPost("join/"+pNumber+"/"+bytesEncoded);
    }

    public static void generateEphemeral(String password) {

        SecretKey key = null;

        try {
            char[] passwordChar = "Apple123".toCharArray();
            PBEKeySpec pbeSpec = new PBEKeySpec(passwordChar, salt, 1000);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            key = keyFactory.generateSecret(pbeSpec);
        } catch (NoSuchAlgorithmException ex) {
        } catch (InvalidKeySpecException ex) {
        }

        ephemeralKey = key;
    }

    public static void generateKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException ex) {
            Log.e("Error", ex.toString());
        }
    }

    private static byte[] encryptConnectionData(String phoneNum) {

        try {
            // Prep cipher
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
            pbeCipher.init(ENCRYPT_MODE, ephemeralKey, new PBEParameterSpec(salt, 1000));

            // Encrypt nonce with pub key of pka (added security)
//            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            rsaCipher.init(ENCRYPT_MODE, pkaPubKey);
//            byte[] nonceBytes = rsaCipher.doFinal(phoneNum.getBytes());

            // Package data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(phoneNum.getBytes());
//            baos.write("---".getBytes());
//            baos.write(Base64.encode(nonceBytes, Base64.NO_WRAP)); // encrypted with private RSA
//            baos.write("---".getBytes());
//            baos.write(Base64.encode(publicKey.getEncoded(), Base64.NO_WRAP));

            // Encrypt and return
            byte[] cipherBytes = pbeCipher.doFinal(baos.toByteArray());

            return cipherBytes;

        } catch (Exception ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }

    public static String encryptAndEncodeString(String message)
    {
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(ENCRYPT_MODE, privateKey);
            byte[] encrpytedMessageBytes = rsaCipher.doFinal(message.getBytes());
            message = encodeToBase64(encrpytedMessageBytes);

            // Split message into parts
            if (message.length() > 160)
            {
                int remainder = message.length() % 160;
            }
        } catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        return message;
    }

    /**
     * Sending Methods
     */

    public static byte[] decodeFromBase64(String cipher) {
        // HTML decode from transport
        cipher = cipher.replace("%2B", "+").replace("%2F", "/");
        // Base64 decode
        return Base64.decode(cipher, Base64.NO_WRAP);
    }

    public static String encodeToBase64(byte[] data) {
        // Encode bytes into base64
        String encodedData = Base64.encodeToString(data, Base64.NO_WRAP);
        // HTML encode for transport
        return encodedData.replace("+", "%2B").replace("/", "%2F");
    }

    public static String doPost(String restMethod) {

        String data = null;

        try {

            URL url = new URL(endpoint + restMethod);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            if ((output = br.readLine()) != null) {
                data = output;
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            Log.e("Error Posting", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Error Posting", e.toString());
            e.printStackTrace();
        }

        return data;
    }



}
