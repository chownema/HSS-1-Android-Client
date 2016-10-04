package com.aut.android.highlysecuretexter.Controller;

import android.util.Log;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.SecretKey;

public class Client implements Serializable {

    private String mobile;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private HashMap<String, Contact> contacts;
    private SecretKey ephemeralKey;
    private boolean pkaConnected;
    private String validationToken;

    public Client(String mobile, SecretKey ephemeralKey) {
        this.mobile = mobile;
        this.ephemeralKey = ephemeralKey;
        contacts = new HashMap<>();
        generateRSAKeys();
        createValidationToken();
    }

    private void generateRSAKeys() {
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

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getMobile() {
        return mobile;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public HashMap<String, Contact> getContacts() {
        return contacts;
    }

    public void addContactInformation(String number, PublicKey pubkey) {
        Contact c =  new Contact(number);
        c.setPublicKey(pubkey);
        contacts.put(number, c);
    }

    public SecretKey getEphemeralKey() {
        return ephemeralKey;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public boolean isPkaConnected() {
        return pkaConnected;
    }

    public void setPkaConnected(boolean pkaConnected) {
        this.pkaConnected = pkaConnected;

        byte[] shit = {1, 2, 3};

        SecureRandom sec = new SecureRandom();
        sec.nextBytes(shit);
    }

    private void createValidationToken() {

        // Encrypt mobile number
        byte[] encryptedMobile = Crypto.encryptRSA(privateKey, mobile.getBytes());
        // Encode for transport
        this.validationToken = Utility.encodeToBase64(encryptedMobile);
    }
}
