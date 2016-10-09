package com.aut.android.highlysecuretexter.Controller;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Adam on 30/09/16.
 * Edited by Miguel on 30/09/16
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

    /**
     * Function which encapsulates the request of a public key of a contact
     * with encryption of the mobile number of the contact with RSA using the
     * clients Private key.
     * @param contactNumber
     * @param clientPrivateKey
     * @return Byte array of the request for a contacts public key
     */
    public static byte[] encryptPublicKeyRequest(String contactNumber, PrivateKey clientPrivateKey)
    {
        // Encrypt nonce/contact's number with Clients Private key
        byte[] nonceBytes = encryptRSA(clientPrivateKey, contactNumber.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            baos.write(Base64.encode(nonceBytes, Base64.NO_WRAP)); // encrypted with private RSA
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /***
     * Function which Encrypts and Encodes a String With AES and Base64.
     * This function will return a wrapped up secure text message to be sent from this
     * client to the contact.
     * This Function will Immediately break the program if message or secret key is
     * null.
     * @param message
     * @param secretKey
     * @return Encrypted and Encoded message String
     */
    public static String encryptAndEncodeAESMessage(String message, SecretKey secretKey)
    {
        String errorMessage = null;
        try
        {  // create a cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // initialize cipher for encryption
            IvParameterSpec initVector = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, initVector);
            // encrypt the plaintext
            byte[] plaintext = message.getBytes();
            byte[] ciphertext = cipher.doFinal(plaintext);
            // base 64 encode the ciphertext as a string
            String encodedString = Base64.encodeToString(ciphertext,
                    Base64.NO_WRAP);
            return encodedString;
        }
        catch (NoSuchAlgorithmException e) {
                errorMessage = "Encryption algorithm not available: " + e;
            } catch (InvalidKeyException e) {
                errorMessage = "Invalid Key: " + e;
            } catch (NoSuchPaddingException e) {
                errorMessage = "Padding scheme not available: " + e;
            } catch (BadPaddingException e) {
                errorMessage = "Bad Padding : " + e;
            } catch (IllegalBlockSizeException e) {
                errorMessage = "Illegal Block size: " + e;
            } catch (InvalidAlgorithmParameterException e) {
                errorMessage = "Invalid Algorithm: " + e;
            }
        return message;
    }

    /**
     * Function which Decrypts and Decodes a received message String With AES and Base64.
     * This function will return an unwrapped plain text message string which was sent by
     * the contact to this client.
     * This Function will Immediately break the program if message or secret key is
     * null.
     * @param encodedmessage
     * @param secretKey
     * @return Decrypted and Decoded message String
     */
    public static String decodeAndDecrypAESMessage(String encodedmessage, SecretKey secretKey)
    {
        String errorMessage = null;
        // base 64 decode the Cipher text as a byte[]
        byte[] ciphertext = Base64.decode(encodedmessage, Base64.NO_WRAP);
        try
        {  // create a cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // initialize cipher for encryption
            IvParameterSpec initVector = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, initVector);
            // decrypt the cipher text
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
     * Used to generate a Secret AES key from a RSA Public key String
     * The Function creates a RSA public key instance with the pubkeystring
     * then takes the first 16 bytes of the key to create an AES secret key
     * which it returns.
     * @param pubKeyString
     * @return Secret AES key for Client to Contact communication
     */
    public static SecretKey generateSecretKey(String pubKeyString) {
        SecretKey sKey = null;

        PublicKey pubKey = generatePublicKey(pubKeyString);
        // Take the first 16 bits the key and return it for AES cipher
        byte sKeyBytes[] = Arrays.copyOf(pubKey.getEncoded(), 16);
        sKey = new SecretKeySpec(sKeyBytes, "AES");

        // Throw run time exception if Key is equal to null
        if (sKey == null)
        {
            throw new RuntimeException("Secret Key Generation Error");
        }

        return sKey;
    }


    /**
     * Generates a Public key by using a public key string
     * @param pKeyString
     * @return Public key generated by a public key String
     */
    public static PublicKey generatePublicKey(String pKeyString) {
        PublicKey pKey  = null;

        try {
            byte[] publicBytes = pKeyString.getBytes();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            pKey = keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Throw run time exception if Key is equal to null
        if (pKey == null)
        {
            throw new RuntimeException("Public Key Generation Error");
        }
        return pKey;
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


    /**
     * generates an encrypted validation package mobile number string encrypted with the private key of the client
     * @param clientPrivateKey
     * @param mobile
     * @return validationPackageString
     */
    public static String encryptValidationPackage(PrivateKey clientPrivateKey, String mobile) {
        String validationPackageString = "";
        // Encrypt Mobile number
        byte[] encryptedBytes = encryptRSA(clientPrivateKey, mobile.getBytes());
        // Put into string
        validationPackageString = new String(encryptedBytes);

        return validationPackageString;
    }


    public static String doubleEncryptData(byte[] data, Key recipientKey) {

        ArrayList<byte[]> chunks = new ArrayList<>();

        // Segment data to encrypt into 100 byte chunks (or remainder)
        for(int i = 0; i < data.length;) {

            int difference = data.length - i;

            if(difference >= 100) {
                byte[] chunk = new byte[100];

                for(int x = 0; x < 100; x++)
                    chunk[x] = data[i++];

                chunks.add(chunk);
            }
            else {
                byte[] chunk = new byte[difference];

                for(int x = 0; x < difference; x++)
                    chunk[x] = data[i++];

                chunks.add(chunk);
            }
        }

        byte[] encryptedChunk = new byte[chunks.size() * 256];
        int counter = 0;

        // Encrypt chunks
        for(int i = 0; i < chunks.size(); i++) {
            try {
                // Get chunk i
                byte[] chunk = chunks.get(i);
                // Encrypt inner layer with pka private key
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding");
                rsaCipher.init(Cipher.ENCRYPT_MODE, recipientKey);
                byte[] inner = rsaCipher.doFinal(chunk);
                // Encrypt outer layer with recipient public key
                Cipher rsaCipher2 = Cipher.getInstance("RSA/ECB/NoPadding");
                rsaCipher2.init(Cipher.ENCRYPT_MODE, Network.pkaPublicKey);
                byte[] outer = rsaCipher2.doFinal(inner);

                for(int x = 0; x < outer.length; x++)
                    encryptedChunk[counter++] = outer[x];
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
                System.out.println("Unable to encrypt data package for transport");
            }
        }

        // Build up contents for transport response of HTTP post
        // "actual bytes length ||| transportation bytes length ||| byte data"
        StringBuilder sb = new StringBuilder();
        sb.append(data.length).append("|||");
        sb.append(encryptedChunk.length).append("|||");
        sb.append(Utility.encodeToBase64(encryptedChunk));

        return sb.toString();
    }

    public static byte[] doubleDecryptData(String data, Key senderKey, Key recipientKey) {

        byte[] decodedData = Utility.decodeFromBase64(data);
        String[] dataSplit = new String(decodedData).split("\\|\\|\\|");
        int actualDataLength = Integer.parseInt(dataSplit[0]);
        int paddedDataLength = Integer.parseInt(dataSplit[1]);
        byte[] decoded = Utility.decodeFromBase64(dataSplit[2]);

        ArrayList<byte[]> encryptedChunks = new ArrayList<>();

        // Break decoded data into padded chunks
        for(int i = 0; i < paddedDataLength / 256; i++) {
            byte[] chunk = new byte[256];

            int upper = (256 * i) + 256;

            for(int y = upper - 256, counter = 0; y < upper; y++)
                chunk[counter++] = decoded[y];

            encryptedChunks.add(chunk);
        }

        // Decrypt chunks
        for(int i = 0; i < encryptedChunks.size(); i++) {
            try {
                // Get chunk
                byte[] chunk = encryptedChunks.get(i);
                // Decrypt outer layer with pka private key
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/NoPadding");
                rsaCipher.init(Cipher.DECRYPT_MODE, recipientKey);
                byte[] outer = rsaCipher.doFinal(chunk);
                // Decrypt inner layer with recipient public key
                Cipher rsaCipher2 = Cipher.getInstance("RSA/ECB/NoPadding");
                rsaCipher2.init(Cipher.DECRYPT_MODE, senderKey);
                byte[] inner = rsaCipher2.doFinal(outer);

                int dataLength = (actualDataLength >= 100) ? 100 : actualDataLength;
                byte[] actualData = new byte[dataLength];

                // Get actual data from chunk
                for(int x = inner.length - dataLength, counter = 0; x < inner.length;)
                    actualData[counter++] = inner[x++];

                // Replace chunk
                encryptedChunks.set(i, actualData);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                System.out.println("Unable to decrypt data package from transport");
            }
        }

        // Concatenate chunks into one byte array
        byte[] dataChunk = new byte[actualDataLength];
        int counter = 0;

        for(byte[] chunk : encryptedChunks) {
            for(byte b : chunk)
                dataChunk[counter++] = b;
        }

        return dataChunk;
    }
}
