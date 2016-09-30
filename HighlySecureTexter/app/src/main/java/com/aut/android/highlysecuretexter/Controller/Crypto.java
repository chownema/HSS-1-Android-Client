package com.aut.android.highlysecuretexter.Controller;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;

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
            //byte[] cipherBytes = pbeCipher.doFinal(baos.toByteArray());
            byte[] cipherBytes = aesCipher.doFinal(baos.toByteArray());
            return cipherBytes;

        } catch (Exception ex) {
            Log.e("Error", ex.toString());
        }
        return null;
    }
}
