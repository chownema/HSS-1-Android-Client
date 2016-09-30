package com.aut.android.highlysecuretexter.Controller;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(ENCRYPT_MODE, pkaPublicKey);
            byte[] nonceBytes = rsaCipher.doFinal(client.getMobile().getBytes());

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
    public static String encryptAndEncodeMessage(String message, SecretKey secretKey)
    {
        assert(secretKey != null || message != null);
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
    public static String decodeAndDecryptMessage(String encodedmessage, SecretKey secretKey)
    {  String errorMessage = null;
        assert(secretKey != null || encodedmessage != null);
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
     * Used to generate a Secret AES key from a RSA Public key
     * @param pubKey
     * @return Secret AES key for Client to Contact communication
     */
    private static SecretKey generateSecretKey (PublicKey pubKey)
    {
        // Take the first 16 bits of the key and return it for AES cipher
        byte[] sKeyBytes = Arrays.copyOf(pubKey.getEncoded(), 16);
        SecretKey sKey = new SecretKeySpec(sKeyBytes, "AES");

        return sKey;
    }

}
