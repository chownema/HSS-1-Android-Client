package com.aut.android.highlysecuretexter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CrossProcessCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.Client;
import com.aut.android.highlysecuretexter.Controller.Contact;
import com.aut.android.highlysecuretexter.Controller.Crypto;
import com.aut.android.highlysecuretexter.Controller.Network;
import com.aut.android.highlysecuretexter.Controller.Utility;

import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class MessagingActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatingActionButton sendMsgButton;
    private EditText inputMessage, cipherMessage, inputNumber;
    static ListView messageListView;
    Intent i;

    // Message List Var
    static ArrayList<String> messageList;
    private static ArrayAdapter<String> adapter;

    // Receiver
    private SMSBroadcastReceiver receivedBroadcastReceiver = null;
    boolean mIsReceiverRegistered = false;

    // Client Object
    static Client client;

    // Contact Var
    PublicKey contactPubkey = null;

    public static String cNumber = "";

    // DEBUG value
    SecretKey sKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);


        i = getIntent();
        cNumber = i.getStringExtra("number");
        // DEBUG
        cNumber = "0211245734";

        final String contactNumber = cNumber;

        setTitle(contactNumber);
        client = (Client) i.getSerializableExtra("client");

        // TODO: Request Public key of contact
        // TODO: Generate secret Key and store in contact object

        // DEBUG Set Key pair
        client = new Client("0211245734", null);
        client.generateRSAKeys();


        // DEBUG init secret key for debug
        sKey = Crypto.generateSecretKey(client.getPublicKey());
//        sKey = Crypto.generateSecretKey(Utility.encodeToBase64(
//                client.getPublicKey().toString().getBytes()));

        // Create new Contact object and setup session key
        Contact c = new Contact(contactNumber);
        c.setSessionKey(sKey);
        // Put into client object
        client.getContacts().put(contactNumber, c);

//        new AsyncTask<Void, Void, Void>()
//        {
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                // Get Contacts Public key
//                contactPubkey = Network.getContactPublicKey(contactNumber, client);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                // Add Contact information and store it in the client object
//                client.addContactInformation(i.getStringExtra("number"), contactPubkey);
//                super.onPostExecute(aVoid);
//            }
//        }.execute();


        // TODO: Need to Send request to contact to initiate conversation

        // TODO: Recieve Aknowledgement and add Secret key given by the contact

        // Init Message List View
        messageList = new ArrayList<>();
        messageListView = (ListView) findViewById(R.id.message_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(adapter);

        // Init Buttons
        sendMsgButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_send);
        sendMsgButton.setOnClickListener(this);

        inputMessage = (EditText) findViewById(R.id.message_edit_text_view);
        // Set Layout to Be pushed up when Soft Keyboard is used
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        /** Init Debug Variables **/
//        Utility.initDebugValues();


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsReceiverRegistered) {
            if (receivedBroadcastReceiver == null)
                receivedBroadcastReceiver = new SMSBroadcastReceiver();
            registerReceiver(receivedBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
            mIsReceiverRegistered = true;
        }
    }

    protected void sendSMSMessage()
    {
        String phoneNum = i.getStringExtra("number");
        phoneNum = cNumber;
        try {
            // Break message into parts and send it
            SmsManager sms = SmsManager.getDefault();
            String ptMsg = inputMessage.getText().toString();
            // TODO: add secret key from contacts hash map

            // Get session key
            SecretKey sessionKey = client.getContacts().get(cNumber).getSessionKey();

            // Send encrypted message
            String msg = Crypto.encryptAndEncodeAESMessage(ptMsg, sessionKey, client.getPrivateKey());
            Log.e("message", msg);
            ArrayList<String> parts = sms.divideMessage(msg);
            sms.sendMultipartTextMessage(phoneNum, null, parts, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent " + msg, Toast.LENGTH_LONG).show();
            // Update the UI after sending SMS
            updateUI(ptMsg, true);
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),
                    "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            Log.e("Failed to send SMS", e.toString());
        }

    }

    public static void updateUI(String msg, boolean sent)
    {
        if (sent)
        {
            // Add it to the list
            messageList.add("You :" + msg);
        }
        else
        {
            messageList.add("Peer : " + msg);
        }
        adapter.notifyDataSetChanged();
        messageListView.smoothScrollToPosition(messageList.size(), messageList.size());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.floatingActionButton_send):{
                // Send a Message
                sendSMSMessage();
            }break;
        }
    }


    /**
     * public static Broadcast Receiver class which on receiving a text message
     * updates the clients Message list. Since Broadcast receivers do not have
     * a way of updating a UI from outside its class it has been implemented here.
     */
    public static class SMSBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {  // obtain the SMS message
            Bundle bundle = intent.getExtras();
            StringBuilder stringBuilder = new StringBuilder();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage message = SmsMessage.createFromPdu
                            ((byte[]) pdus[i]);
                    String receivedString = message.getDisplayMessageBody();

                    //TODO: use actual public key when key exchange done
                    // Get public key from client object
                    //PublicKey contactPublicKey = client.getContacts().get(cNumber).getPublicKey();

                    // Debug key
                    PublicKey contactPublicKey = client.getPublicKey();

                    // Get session key
                    SecretKey sessionKey = client.getContacts().get(cNumber).getSessionKey();

                    // Get decrypted String message
                    String[] decryptedMessage = Crypto.decodeAndDecrypAESMessage(receivedString, sessionKey
                            , contactPublicKey);

                    stringBuilder.append(decryptedMessage[0] + ", " + decryptedMessage[1]);
                }

                Toast toast = Toast.makeText(context,
                        stringBuilder.toString(), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(context,
                        "Error: no message data received", Toast.LENGTH_SHORT);
                toast.show();
            }

            if(bundle !=null)
                updateUI(stringBuilder.toString(), false);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receivedBroadcastReceiver);
        super.onDestroy();
    }
}


