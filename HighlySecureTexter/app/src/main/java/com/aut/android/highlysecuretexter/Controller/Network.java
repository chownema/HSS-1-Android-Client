package com.aut.android.highlysecuretexter.Controller;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Network {

    public final static String endpoint = "http://172.28.56.205:8080/PKAServer/webresources/pka/";
    public static PublicKey pkaPublicKey = null;

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

    public static void connectToPKA(Client client) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // get pka key
        String response = doPost("pkakey");
        byte[] pkaPublicKeyBytes = Utility.decodeFromBase64(response);
        pkaPublicKey = KeyFactory.getInstance("RSA").generatePublic
                (new X509EncodedKeySpec(pkaPublicKeyBytes));

        // Check PKA Key is null
        if (pkaPublicKey == null) {
            throw new RuntimeException("Key Error");
        }

        // Create Encrypted encrypted data
        byte[] encryptedConnPackage = Crypto.encryptInitialConnection(client, pkaPublicKey);

        // Base64 encode encrypted package into a string
        String bytesEncoded = Utility.encodeToBase64(encryptedConnPackage);

        // Send request to join to PKA
        response = doPost("join/" + client.getMobile() + "/" + bytesEncoded);

        // Base64 decode
        // Decrypt response with pka pub key
        // Decrypt inner response with self pri key
        // Interpret contained value (nonce) as mobile number / 2

        // TODO: read data received from above POST and check that nonce has been divided by 2
        if(response.equals("Success"))
            client.setPkaConnected(true);
    }

    public static SecretKey getEphemeralKey(String mobile) {

        String response = doPost("request/" + mobile);
        byte[] keyData = Utility.decodeFromBase64(response);
        return new SecretKeySpec(keyData, "AES");
    }

    public static String[] updateContacts(Client client) {

        String response = doPost("numbers/" + client.getMobile());
        return response.split(", ");
    }
}
