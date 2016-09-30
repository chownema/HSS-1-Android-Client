//package com.aut.android.highlysecuretexter;
//
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v7.app.AppCompatActivity;
//import android.telephony.SmsManager;
//import android.telephony.SmsMessage;
//import android.util.Log;
//import android.view.View;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.WindowManager;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import com.aut.android.highlysecuretexter.Controller.SMSBroadcastReceiver;
//import com.aut.android.highlysecuretexter.Controller.Utility;
//
//import java.util.ArrayList;
//
//public class MessagingActivity extends AppCompatActivity implements View.OnClickListener {
//
//    private FloatingActionButton sendMsgButton;
//    private EditText inputMessage, cipherMessage, inputNumber;
//    static ListView messageListView;
//    Intent i;
//
//    // Message List Var
//    static ArrayList<String> messageList;
//    private static ArrayAdapter<String> adapter;
//
//    // Receiver
//    private SMSBroadcastReceiver receivedBroadcastReceiver = null;
//    boolean mIsReceiverRegistered = false;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_messaging);
//
//        // Add Recipient Number to the title
//        i = getIntent();
//        setTitle(i.getStringExtra("number"));
//
//        // Init Message List View
//        messageList = new ArrayList<>();
//        messageListView = (ListView) findViewById(R.id.message_list_view);
//        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
//        messageListView.setAdapter(adapter);
//
//        // Init Buttons
//        sendMsgButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_send);
//        sendMsgButton.setOnClickListener(this);
//
//        inputMessage = (EditText) findViewById(R.id.message_edit_text_view);
//        // Set Layout to Be pushed up when Soft Keyboard is used
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//
//        /** Init Debug Variables **/
//        Utility.initDebugValues();
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!mIsReceiverRegistered) {
//            if (receivedBroadcastReceiver == null)
//                receivedBroadcastReceiver = new SMSBroadcastReceiver();
//            registerReceiver(receivedBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
//            mIsReceiverRegistered = true;
//        }
//    }
//
//    protected void sendSMSMessage()
//    {
//        String phoneNum = i.getStringExtra("number");
//        try {
//            // Break message into parts and send it
//            SmsManager sms = SmsManager.getDefault();
//            String ptMsg = inputMessage.getText().toString();
//            String msg = Utility.encryptAndEncodeString(ptMsg);
//            ArrayList<String> parts = sms.divideMessage(msg);
//            sms.sendMultipartTextMessage(phoneNum, null, parts, null, null);
//            Toast.makeText(getApplicationContext(), "SMS sent " + msg, Toast.LENGTH_LONG).show();
//            // Update the UI after sending SMS
//            updateUI(ptMsg, true);
//        }
//        catch (Exception e)
//        {
//            Toast.makeText(getApplicationContext(),
//                    "SMS failed, please try again.", Toast.LENGTH_LONG).show();
//        }
//
//    }
//
//    public static void updateUI(String msg, boolean sent)
//    {
//        if (sent)
//        {
//            // Add it to the list
//            messageList.add("You :" + msg);
//        }
//        else
//        {
//            messageList.add("Peer : " + msg);
//        }
//        adapter.notifyDataSetChanged();
//        messageListView.smoothScrollToPosition(messageList.size(), messageList.size());
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch(v.getId()){
//            case (R.id.floatingActionButton_send):{
//                sendSMSMessage();
//            }break;
//        }
//    }
//
//    public static class SMSBroadcastReceiver extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {  // obtain the SMS message
//            Bundle bundle = intent.getExtras();
//            StringBuilder stringBuilder = new StringBuilder();
//            if (bundle != null) {
//                Object[] pdus = (Object[]) bundle.get("pdus");
//                for (int i = 0; i < pdus.length; i++) {
//                    SmsMessage message = SmsMessage.createFromPdu
//                            ((byte[]) pdus[i]);
//                    String receivedString = message.getDisplayMessageBody();
//                    String decryptedMessage = Utility.decodeAndDecryptString(receivedString);
//                    stringBuilder.append(decryptedMessage);
//                }
//
//                Toast toast = Toast.makeText(context,
//                        stringBuilder.toString(), Toast.LENGTH_SHORT);
//                toast.show();
//            } else {
//                Toast toast = Toast.makeText(context,
//                        "Error: no message data received", Toast.LENGTH_SHORT);
//                toast.show();
//            }
//
//            if(bundle !=null)
//                updateUI(stringBuilder.toString(), false);
//        }
//    }
//}
//
//
