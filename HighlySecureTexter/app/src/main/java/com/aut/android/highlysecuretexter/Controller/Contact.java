package com.aut.android.highlysecuretexter.Controller;

import javax.crypto.SecretKey;

public class Contact {

    private String mobile;
    private SecretKey publicKey;
    // TODO: Add AES key

    public Contact(String mobile) {
        this.mobile = mobile;
    }

    public SecretKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(SecretKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getMobile() {
        return mobile;
    }
}
