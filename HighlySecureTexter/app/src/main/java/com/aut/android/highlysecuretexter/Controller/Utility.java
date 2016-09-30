package com.aut.android.highlysecuretexter.Controller;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
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

//
//    // Aes Encryption
//    private static IvParameterSpec initVector;
//    private static byte[] ivBytes = { 1, -2, 3, -4, 5, -6, 7, -8, 9,
//            -10, 11, -12, 13, -14, 15, -16 }; // random array of 16 bytes
//    private static SecretKey secretKey;
//
//    // Debug variables
//    public static PrivateKey privateKeyB = null;
//    public static PublicKey publicKeyB = null;
//
//    /**
//     * Debugging Functions
//     */
//
//
//    public static void initDebugValues()
//    {
//        // Initialize debug values
//
//        // Encrypted Messaging test
//        // Init RSA messaging variables
//        initMessagingEncryption();
//        // Create 2 local key pairs for messaging
//        generateDebugKeys();
//        generateKeys();
//    }
//
//    public static void generateDebugKeys() {
//        try {
//            // Generate key pair for B
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//            kpg.initialize(2048);
//            KeyPair keyPair = kpg.generateKeyPair();
//            publicKeyB = keyPair.getPublic();
//            privateKeyB = keyPair.getPrivate();
//        } catch (NoSuchAlgorithmException ex) {
//            Log.e("Error", ex.toString());
//        }
//    }
//
//    /**
//     * Encryption Functions
//     */
//
//    public static void initMessagingEncryption()
//    {
//        // Initialize messaging values
//
//        // init iv for AES cipher
//        initVector = new IvParameterSpec(ivBytes);
//
//    }
//
//
//    /**
//     * Used to generate a secret key from a RSA Public key
//     * @param pubKey
//     * @return
//     */
//    private static SecretKey generateSecretKey (PublicKey pubKey)
//    {
//        // Take the first 16 bits of the key and return it for AES cipher
//        byte[] sKeyBytes = Arrays.copyOf(pubKey.getEncoded(), 16);
//        SecretKey sKey = new SecretKeySpec(sKeyBytes, "AES");
//
//        return sKey;
//    }
//
//
//
//
//
//    public static String encryptAndEncodeString(String message)
//    {
//        try
//        {  // create a cipher
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            // initialize cipher for encryption
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey, initVector);
//            // encrypt the plaintext
//            byte[] plaintext = message.getBytes();
//            byte[] ciphertext = cipher.doFinal(plaintext);
//            // base 64 encode the ciphertext as a string
//            String encodedString = Base64.encodeToString(ciphertext,
//                    Base64.NO_WRAP);
//            return encodedString;
//        }
//        catch (NoSuchAlgorithmException e)
//            {
//                e.printStackTrace();
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//            } catch (BadPaddingException e) {
//                e.printStackTrace();
//            } catch (IllegalBlockSizeException e) {
//                e.printStackTrace();
//            } catch (InvalidAlgorithmParameterException e) {
//                e.printStackTrace();
//            }
//            return message;
//    }
//
//    public static String decodeAndDecryptString(String encodedString)
//    {  String errorMessage = null;
//        // base 64 decode the Cipher text as a byte[]
//        byte[] ciphertext = Base64.decode(encodedString, Base64.NO_WRAP);
//        try
//        {  // create a cipher
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            // initialize cipher for encryption
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, initVector);
//            // decrypt the ciphertext
//            byte[] deciphertext = cipher.doFinal(ciphertext);
//            return new String(deciphertext);
//        }
//        catch (NoSuchAlgorithmException e)
//        {  errorMessage = "Encryption algorithm not available: " + e;
//        }
//        catch (NoSuchPaddingException e)
//        {  errorMessage = "Padding scheme not available: " + e;
//        }
//        catch (InvalidKeyException e)
//        {  errorMessage = "Invalid key: " + e;
//        }
//        catch (InvalidAlgorithmParameterException e)
//        {  errorMessage = "Invalid algorithm parameter: " + e;
//        }
//        catch (IllegalBlockSizeException e)
//        {  errorMessage = "Cannot pad plaintext: " + e;
//        }
//        catch (BadPaddingException e)
//        {  errorMessage = "Exception with padding: " + e;
//        }
//        Log.e("Error", errorMessage);
//        return null;
//    }

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

    public static Object deserialize(byte[] data) {

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
