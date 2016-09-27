package com.aut.android.highlysecuretexter.Controller;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import cz.msebera.android.httpclient.client.methods.HttpPatch;

import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Created by MI on 27/09/16.
 */

public class ClientHelper{

    private String phoneNum;
    private String oneTimeKey;
    private SecretKey ephemeralKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey pkaPubKey;
    private byte[] cipherByteArray;
    private byte[] bytesEncoded;
    private final byte[] salt = {-84, 40, -10, -53, -80, 90, -57, 125};

    private HttpHelper h;

    private Context context;

    public ClientHelper(String phoneNum, String oneTimeKey, Context c) throws InvalidKeySpecException, NoSuchAlgorithmException {
        this.phoneNum = phoneNum;
        this.oneTimeKey = oneTimeKey;
        this.context = c;
        setPKAKey();
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
            final Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES/CBC/PKCS5Padding");
            pbeCipher.init(ENCRYPT_MODE, ephemeralKey, new PBEParameterSpec(salt, 1000));

            // Encrypt nonce with pub key of pka (added security)
            final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // Wait receive PKA public Key
            new AsyncTask<Void, Void, Void>()
            {
                // Wait for PKA PUB KEY
                @Override
                protected Void doInBackground(Void... voids)
                {
                    while(getPKAKey() == null)
                    {
                        // wait
                    }
                    try {
                        initCipherBytes(getPKAKey(), rsaCipher, pbeCipher);
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                // After Received Key continue
                @Override
                protected void onPostExecute(Void aVoid)
                {
                    // Complete initialisation join message
                    byte[] cipherBytes = cipherByteArray;
                    Log.e("Size of cipher", ""+cipherBytes.length);
                    String bytesEncoded = Base64.encodeToString(cipherBytes, Base64.DEFAULT);
                    // Replace for http safe characters before sending it off
                    bytesEncoded = bytesEncoded.replace("+", "%2B");
                    bytesEncoded = bytesEncoded.replace("/", "%2F");
                    super.onPostExecute(aVoid);
                }
            }.execute();
        } catch (NoSuchAlgorithmException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        } catch (InvalidAlgorithmParameterException ex) {
            Log.e("Error", ex.toString());
            ex.printStackTrace();
        }
        return null;
    }






    public byte[] initCipherBytes(PublicKey pkaKey, Cipher rsaCipher, Cipher pbeCipher) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {

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
        // set Cipher Byte Array
        cipherByteArray = cipherBytes;
        // Return Cipher Byte Array
        return cipherBytes;
    }





    public void setPKAKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        h = new HttpHelper(context);
        h.post("pkakey");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while(h.getResponse() == null) {
                    //wait for response from server
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // Set new PKA Key
                Log.e("Key got back", h.getResponse());
                byte[] publicBytes = Base64.decode(h.getResponse(), Base64.DEFAULT);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                KeyFactory keyFactory = null;
                try {
                    keyFactory = KeyFactory.getInstance("RSA");
                    pkaPubKey = keyFactory.generatePublic(keySpec);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    public PublicKey getPKAKey()
    {
        return pkaPubKey;
    }

    // Sends join message to the PKA
    public Void sendJoin() {
        h = new HttpHelper(context);
        h.post("pkakey");

        new AsyncTask<Void, Void, Void> () {

            @Override
            protected Void doInBackground(Void... voids) {
                while(h.getResponse() == "" || h.getResponse() == null)
                {
                    // wait
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                generateEphemeral();
                generateKeys();
                // set data package to send
                encryptDetails();
            }
        }.execute();


        return null;
    }
}

