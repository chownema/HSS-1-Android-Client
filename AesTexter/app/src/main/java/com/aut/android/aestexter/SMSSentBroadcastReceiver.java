/**
 * BroadcastReceiver that is notified when SMS message is sent
 * @see AndroidSMSDemoActivity.java
 */
package com.aut.android.aestexter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSSentBroadcastReceiver extends BroadcastReceiver
{
   public void onReceive(Context context, Intent intent)
   {  Toast toast;
      switch (getResultCode())
      {  case Activity.RESULT_OK :
            toast = Toast.makeText(context, "SMS has been sent",
               Toast.LENGTH_SHORT);
            toast.show();
            break;
         case SmsManager.RESULT_ERROR_GENERIC_FAILURE :
            toast = Toast.makeText(context,
               "SMS had a generic failure", Toast.LENGTH_SHORT);
            toast.show();
            break;
         case SmsManager.RESULT_ERROR_NO_SERVICE :
            toast = Toast.makeText(context,
               "SMS service currently unavailable",Toast.LENGTH_SHORT);
            toast.show();
            break;
         case SmsManager.RESULT_ERROR_NULL_PDU :
            toast = Toast.makeText(context, "SMS has no PDU provided",
               Toast.LENGTH_SHORT);
            toast.show();
            break;
         case SmsManager.RESULT_ERROR_RADIO_OFF :
            toast = Toast.makeText(context,
               "SMS does not have radio on", Toast.LENGTH_SHORT);
            toast.show();
            break;
      }
   }
}
