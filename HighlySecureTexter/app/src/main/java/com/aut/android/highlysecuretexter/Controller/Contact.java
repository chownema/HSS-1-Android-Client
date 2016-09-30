package com.aut.android.highlysecuretexter.Controller;

import java.security.PublicKey;

import javax.crypto.SecretKey;

public class Contact {

    private String mobile;
    // TODO: Add AES key
    private SecretKey sessionKey;
    private PublicKey publicKey;


    public Contact(String mobile) {
        this.mobile = mobile;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(SecretKey publicKey) {
        this.sessionKey = publicKey;
    }

    public String getMobile() {
        return mobile;
    }

    public PublicKey getPublicKey() { return publicKey; }

    public void setPublicKey(PublicKey publicKey) { this.publicKey = publicKey; }
}
