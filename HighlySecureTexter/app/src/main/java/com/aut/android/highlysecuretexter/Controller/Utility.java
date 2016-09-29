package com.aut.android.highlysecuretexter.Controller;

import android.content.Context;
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
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Cipher.ENCRYPT_MODE;


/**
 * Created by MI on 28/09/16.
 */

public class Utility {

    public final static byte[] salt = {-84, 40, -10, -53, -80, 90, -57, 125};
    public final static String endpoint = "http://192.168.20.2:8080/PKAServer/webresources/pka/";

    // One off Key
    public static SecretKey ephemeralKey = null;

    // Keys for PKA
    public static PublicKey pkaPubKey = null;
    public static PrivateKey privateKey = null;
    public static PublicKey publicKey = null;

    // Aes Encryption
    private static IvParameterSpec initVector;
    private static byte[] ivBytes = { 1, -2, 3, -4, 5, -6, 7, -8, 9,
            -10, 11, -12, 13, -14, 15, -16 }; // random array of 16 bytes
    private static SecretKey secretKey;

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

    public static void connectToPKA(String pNumber, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // get pka key
        pkaPubKey = KeyFactory.getInstance("RSA").generatePublic
                (new X509EncodedKeySpec(decodeFromBase64(doPost("pkakey"))));

        // Check PKA Key is null
        if (pkaPubKey == null) {
            throw new RuntimeException("Key Error");
        }

        // Get password and generate one time key
        //generateEphemeral(password);

        // Generate Clients key pair
        generateKeys();

        // Generate an AES secret key
        secretKey = generateSecretKey(publicKey);


        // Create Encrypted encrypted data
        byte[] encryptedConnPackage = encryptConnectionData(pNumber);

        // Base64 encode encrypted package into a string
        String bytesEncoded = encodeToBase64(encryptedConnPackage);

        // Send request to join to PKA
        doPost("join/"+pNumber+"/"+bytesEncoded);
    }

    /**
     * Used to generate a secret key from a RSA Public key
     * @param pubKey
     * @return
     */
    private static SecretKey generateSecretKey (PublicKey pubKey)
    {
        // Take the first 16 bits of the key and return it for AES cipher
        byte[] sKeyBytes = Arrays.copyOf(pubKey.getEncoded(), 16);
        SecretKey sKey = new SecretKeySpec(sKeyBytes, "AES");

        return sKey;
    }

    public static String[] getContacts(String mobile) {

        String response = doPost("numbers/" + mobile);
        return response.split(", ");
    }

    public static void generateEphemeral(String password) {

        SecretKey key = null;

        try {
            char[] passwordChar = password.toCharArray();
            PBEKeySpec pbeSpec = new PBEKeySpec(passwordChar, salt, 1000);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            key = keyFactory.generateSecret(pbeSpec);
        } catch (Exception ex) {
            Log.e("Error", ex.toString());
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
            //Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
            //pbeCipher.init(ENCRYPT_MODE, ephemeralKey, new PBEParameterSpec(salt, 1000));

            // Encrypt nonce with pub key of pka (added security)
            //Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            //rsaCipher.init(ENCRYPT_MODE, pkaPubKey);
            //byte[] nonceBytes = rsaCipher.doFinal(phoneNum.getBytes());

            // Package data
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //baos.write(phoneNum.getBytes());
            //baos.write("---".getBytes());
            //baos.write(Base64.encode(nonceBytes, Base64.NO_WRAP)); // encrypted with private RSA
            //baos.write("---".getBytes());
            //baos.write(Base64.encode(publicKey.getEncoded(), Base64.NO_WRAP));

            // Encrypt and return
            //byte[] cipherBytes = pbeCipher.doFinal(baos.toByteArray());
            return publicKey.getEncoded();

        } catch (Exception ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }

    public static String encryptAndEncodeString(String message)
    {
        try
        {  // create a cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // initialize cipher for encryption
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, initVector);
            // encrypt the plaintext
            byte[] plaintext = message.getBytes();
            byte[] ciphertext = cipher.doFinal(plaintext);
            // base 64 encode the ciphertext as a string
            String encodedString = Base64.encodeToString(ciphertext,
                    Base64.NO_WRAP);
            return encodedString;
        }
        catch (NoSuchAlgorithmException e)
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
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            return message;
    }

    public static String decodeAndDecryptString(String encodedString)
    {  String errorMessage = null;
        // base 64 decode the Cipher text as a byte[]
        byte[] ciphertext = Base64.decode(encodedString, Base64.NO_WRAP);
        try
        {  // create a cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // initialize cipher for encryption
            cipher.init(Cipher.DECRYPT_MODE, secretKey, initVector);
            // decrypt the ciphertext
            byte[] deciphertext = cipher.doFinal(ciphertext);
            return new String(deciphertext);
        }
        catch (NoSuchAlgorithmException e)
        {  errorMessage = "Encryption algorithm not available: " + e;
        }
        catch (NoSuchPaddingException e)
        {  errorMessage = "Padding scheme not available: " + e;
        }
        catch (InvalidKeyException e)
        {  errorMessage = "Invalid key: " + e;
        }
        catch (InvalidAlgorithmParameterException e)
        {  errorMessage = "Invalid algorithm parameter: " + e;
        }
        catch (IllegalBlockSizeException e)
        {  errorMessage = "Cannot pad plaintext: " + e;
        }
        catch (BadPaddingException e)
        {  errorMessage = "Exception with padding: " + e;
        }
        Log.e("Error", errorMessage);
        return null;
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
