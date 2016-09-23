/**
 * Android Activity that sends SMS messages which it has encrypted
 * using AES and Base64 encoded as a String
 * Note this example uses a hard-coded secret key for simplicity
 * @author Andrew Ensor
 */
package com.aut.android.aestexter;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidAESDemoActivity extends Activity implements
   OnClickListener
{
    Button setKeyButton;
   FloatingActionButton fab;
   private SMSSentBroadcastReceiver sentBroadcastReceiver;
   private SMSDeliveredBroadcastReceiver deliveredBroadcastReceiver;
   private byte[] keyBytes = { 51, 50, 7, -19, 120, 111, -110, 52, 9,
      -21, -6, -15, -95, 117, 36, -89 }; // the secret key
   private byte[] ivBytes = { 1, -2, 3, -4, 5, -6, 7, -8, 9,
      -10, 11, -12, 13, -14, 15, -16 }; // random array of 16 bytes
   static SecretKeySpec key;
   private IvParameterSpec initVector;
   private final String SMS_SENT_ACTION = "SMS_SENT";
   private final String SMS_DELIVERED_ACTION = "SMS_DELIVERED";

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       ActivityCompat.requestPermissions(this,
               new String[]{Manifest.permission.SEND_SMS}, 1);
       // obtain reference to send button
      setKeyButton = (Button) findViewById(R.id.set_key_button);
       setKeyButton.setOnClickListener(this);
      fab = (FloatingActionButton) findViewById(R.id.fab);
      fab.setOnClickListener(this);

       // Add the key
       TextView keyTextView
               = (TextView)findViewById(R.id.key_text);
       String keyString = keyTextView.getText().toString();

       byte[] bkey;
       try {
           bkey = (keyString).getBytes("UTF-8");
           MessageDigest sha = MessageDigest.getInstance("SHA-1");
           bkey = sha.digest(bkey);
           bkey = Arrays.copyOf(bkey, 16); // use only first 128 bit
           key = new SecretKeySpec(bkey, "AES");
       } catch (UnsupportedEncodingException e) {
           e.printStackTrace();
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
       }

      initVector = new IvParameterSpec(ivBytes);
    }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {  // Inflate the menu; this adds items to action bar if present
      //getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
   }

   /** Called when the activity is started. */
   @Override
   public void onStart()
   {  super.onStart();
      // create broadcast receivers that get notified when SMS sent
      // or delivered
      sentBroadcastReceiver = new SMSSentBroadcastReceiver();
      registerReceiver(sentBroadcastReceiver,
         new IntentFilter(SMS_SENT_ACTION));
      deliveredBroadcastReceiver = new SMSDeliveredBroadcastReceiver();
      registerReceiver(deliveredBroadcastReceiver,
         new IntentFilter(SMS_DELIVERED_ACTION));
   }

   /** Called when the activity is stopped. */
   @Override
   public void onStop()
   {  super.onDestroy();
      if (sentBroadcastReceiver != null)
         unregisterReceiver(sentBroadcastReceiver);
      if (deliveredBroadcastReceiver != null)
         unregisterReceiver(deliveredBroadcastReceiver);
   }

   // implementation of OnClickListener method
   public void onClick(View view)
   {  if (view == fab)
      {  TextView numberTextView
            = (TextView)findViewById(R.id.number_text);
         String numberString = numberTextView.getText().toString();
         TextView messageTextView
            = (TextView)findViewById(R.id.message_text);
         String messageString = messageTextView.getText().toString();
         // send the sms message
         PendingIntent sentPendingIntent = PendingIntent.getBroadcast
            (this, 0, new Intent(SMS_SENT_ACTION), 0);
         PendingIntent deliveredPendingIntent
            = PendingIntent.getBroadcast(this, 0,
            new Intent(SMS_DELIVERED_ACTION), 0);
         SmsManager smsManager = SmsManager.getDefault();
         String sendString = encryptAndEncodeString(messageString);
         if (sendString != null)
            smsManager.sendTextMessage(numberString, null, sendString,
               sentPendingIntent, deliveredPendingIntent);
      }
       // Set the key
       if (view == setKeyButton)
       {
           TextView keyTextView
               = (TextView)findViewById(R.id.key_text);
           String keyString = keyTextView.getText().toString();

           byte[] bkey;
           try {
               bkey = (keyString).getBytes("UTF-8");
               MessageDigest sha = MessageDigest.getInstance("SHA-1");
               bkey = sha.digest(bkey);
               bkey = Arrays.copyOf(bkey, 16); // use only first 128 bit
               key = new SecretKeySpec(bkey, "AES");
           } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
           }
       }
   }

   private String encryptAndEncodeString(String message)
   {  String errorMessage = null;
      try
      {  // create a cipher
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         // initialize cipher for encryption
         cipher.init(Cipher.ENCRYPT_MODE, key, initVector);
         // encrypt the plaintext
         byte[] plaintext = message.getBytes();
         byte[] ciphertext = cipher.doFinal(plaintext);
         // base 64 encode the ciphertext as a string
         String encodedString = Base64.encodeToString(ciphertext,
            Base64.DEFAULT);
         return encodedString;
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
      Toast toast = Toast.makeText(this, errorMessage,
         Toast.LENGTH_SHORT);
      toast.show();
      return null;
   }
}