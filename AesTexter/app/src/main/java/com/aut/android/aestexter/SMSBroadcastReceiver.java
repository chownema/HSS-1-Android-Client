/**
 * BroadcastReceiver that is notified when an SMS message is received
 * which it Base64 decodes and decrypts using AES with a hardcoded key
 * Note this BroadcastReceiver is statically registered in
 * AndroidManifest.xml and application requires permission
 * android.permission.RECEIVE_SMS 
 * @see AndroidAESDemoActivity.java
 */
package com.aut.android.aestexter;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.aut.android.aestexter.AndroidAESDemoActivity;

public class SMSBroadcastReceiver extends BroadcastReceiver
{
   private byte[] keyBytes = { 51, 50, 7, -19, 120, 111, -110, 52, 9,
      -21, -6, -15, -95, 117, 36, -89 }; // the secret key
   private byte[] ivBytes = { 1, -2, 3, -4, 5, -6, 7, -8, 9,
      -10, 11, -12, 13, -14, 15, -16 }; // random array of 16 bytes
   private SecretKeySpec key;
   private IvParameterSpec initVector;

   public SMSBroadcastReceiver()
   {

       key = AndroidAESDemoActivity.key;
      initVector = new IvParameterSpec(ivBytes);
   }

   public void onReceive(Context context, Intent intent)
   {  // obtain the SMS message
      Bundle bundle = intent.getExtras();
      if (bundle != null)
      {  Object[] pdus = (Object[]) bundle.get("pdus");
         StringBuilder stringBuilder = new StringBuilder();
         for (int i = 0; i < pdus.length; i++)
         {  SmsMessage message = SmsMessage.createFromPdu
               ((byte[]) pdus[i]);
            String senderAddress
               = message.getDisplayOriginatingAddress();
            String receivedString = message.getDisplayMessageBody();
            String messageString = decodeAndDecryptString(context,
               receivedString);
            stringBuilder.append("Received Encrypted SMS:\n");
            stringBuilder.append("  Sender: ").append(senderAddress);
            stringBuilder.append(" Message: ").append(messageString);
            stringBuilder.append("\n");
         }
         Toast toast = Toast.makeText(context,
            stringBuilder.toString(), Toast.LENGTH_SHORT);
         toast.show();
      }
      else
      {  Toast toast = Toast.makeText(context,
            "Error: no message data received", Toast.LENGTH_SHORT);
         toast.show();
      }
   }

   private String decodeAndDecryptString(Context context,
      String encodedString)
   {  String errorMessage = null;
      // base 64 decode the ciphertext as a byte[]
      byte[] ciphertext = Base64.decode(encodedString, Base64.DEFAULT);
      try
      {  // create a cipher
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         // initialize cipher for encryption
         cipher.init(Cipher.DECRYPT_MODE, key, initVector);
         // decrypt the ciphertext
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
      Toast toast = Toast.makeText(context, errorMessage,
         Toast.LENGTH_SHORT);
      toast.show();
      Log.e("Error", errorMessage);
      return null;
   }
}