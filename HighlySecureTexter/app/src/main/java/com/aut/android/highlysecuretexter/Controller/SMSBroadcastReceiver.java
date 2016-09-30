///**
// * BroadcastReceiver that is notified when an SMS message is received
// * which it Base64 decodes and decrypts using AES with a hardcoded key
// * Note this BroadcastReceiver is statically registered in
// * AndroidManifest.xml and application requires permission
// * android.permission.RECEIVE_SMS
// * @see AndroidAESDemoActivity.java
// */
//package com.aut.android.highlysecuretexter.Controller;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.telephony.SmsMessage;
//import android.util.Log;
//import android.widget.Toast;
//
//
//public class SMSBroadcastReceiver extends BroadcastReceiver
//{
//    public void onReceive(Context context, Intent intent)
//   {  // obtain the SMS message
//      Bundle bundle = intent.getExtras();
//      if (bundle != null)
//      {  Object[] pdus = (Object[]) bundle.get("pdus");
//         StringBuilder stringBuilder = new StringBuilder();
//         for (int i = 0; i < pdus.length; i++)
//         {  SmsMessage message = SmsMessage.createFromPdu
//               ((byte[]) pdus[i]);
//            String senderAddress
//               = message.getDisplayOriginatingAddress();
//            String receivedString = message.getDisplayMessageBody();
//             String decryptedMessage = Utility.decodeAndDecryptString(receivedString);
//
//             Log.e("Message Received", "Message :: " + decryptedMessage);
//             stringBuilder.append("From :" + senderAddress);
//             stringBuilder.append("Message : ");
//             stringBuilder.append(decryptedMessage);
//         }
//
//         Toast toast = Toast.makeText(context,
//            stringBuilder.toString(), Toast.LENGTH_SHORT);
//         toast.show();
//      }
//      else
//      {  Toast toast = Toast.makeText(context,
//            "Error: no message data received", Toast.LENGTH_SHORT);
//         toast.show();
//      }
//   }
//
//
//
//}