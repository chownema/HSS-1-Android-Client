package com.aut.android.highlysecuretexter;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.Client;
import com.aut.android.highlysecuretexter.Controller.Contact;
import com.aut.android.highlysecuretexter.Controller.Crypto;
import com.aut.android.highlysecuretexter.Controller.Network;
import com.aut.android.highlysecuretexter.Controller.Utility;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.SecretKey;

public class MessagingActivity extends AppCompatActivity implements View.OnClickListener {

    // UI elements
    private FloatingActionButton sendMsgButton;
    private Button sessionButton;
    private EditText inputMessage;
    private CheckBox encryptedCheckBox, signedCheckBox;
    static ListView messageListView;

    // Message List Var
    static ArrayList<String> messageList;
    private static ArrayAdapter<String> adapter;

    // Receiver
    private SMSBroadcastReceiver receivedBroadcastReceiver = null;
    boolean mIsReceiverRegistered = false;

    // Client Object
    Client client;
    Contact contact;
    static String contactMobile;

    // Flags
    static boolean gotPublicKey = false;
    static boolean confirmedAES = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Set SMS permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS}, 1);
        // Get contacts mobile
        contactMobile = getIntent().getStringExtra("number");
        setTitle(contactMobile);
        // Get client
        client = (Client) getIntent().getSerializableExtra("client");
        // Get contact from client
        contact = client.getContacts().get(contactMobile);

        receivedBroadcastReceiver = new SMSBroadcastReceiver(this);
        registerReceiver(receivedBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        mIsReceiverRegistered = true;

        // Init Message List View
        messageList = new ArrayList<>();
        messageListView = (ListView) findViewById(R.id.message_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        messageListView.setAdapter(adapter);

        // Init Buttons
        sendMsgButton = (FloatingActionButton) findViewById(R.id.floatingActionButton_send);
        sendMsgButton.setOnClickListener(this);
        sessionButton = (Button) findViewById(R.id.sessionButton);
        sessionButton.setOnClickListener(this);

        inputMessage = (EditText) findViewById(R.id.message_edit_text_view);
        // Set Layout to Be pushed up when Soft Keyboard is used
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsReceiverRegistered) {
            if (receivedBroadcastReceiver == null) {
                receivedBroadcastReceiver = new SMSBroadcastReceiver(this);
                registerReceiver(receivedBroadcastReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
                mIsReceiverRegistered = true;
            }
        }
    }

    protected void sendSMSMessage(String message, boolean output)
    {
        try {
            // Break message into parts and send it
            SmsManager sms = SmsManager.getDefault();

            String encrypted = null;

            if(confirmedAES)
                encrypted = Crypto.encryptAndEncodeAESMessage(message, contact.getSessionKey());
            else
                encrypted = message;

            ArrayList<String> parts = sms.divideMessage(encrypted);
            sms.sendMultipartTextMessage(contactMobile, null, parts, null, null);
            if(output)
                // Update the UI after sending SMS
                updateUI("You said: " + message);
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),
                    "SMS failed: " + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    public static void updateUI(String message)
    {
        messageList.add(message);

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
                if(gotPublicKey)
                    sendSMSMessage(inputMessage.getText().toString(), true);
                else
                    Toast.makeText(MessagingActivity.this, "No session key found", Toast.LENGTH_SHORT).show();
                break;
            }
            case (R.id.sessionButton): {
                // Get private key
                updateUI("System said: requesting public key");
                createNewSession(false);
                break;
            }
        }
    }

    private void createNewSession(final boolean publicKeyOnly) {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    // Get Contacts Public key
                    PublicKey tempKey = Network.getContactPublicKey(contactMobile, client);

                    if(tempKey != null) {
                        setContactPublicKey(tempKey);
                    }
                }
                catch (Exception ex) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                try {
                    if(contact.getPublicKey() != null) {
                        updateUI("System said: received public key");

                        if(!publicKeyOnly) {
                            updateUI("System said: creating session");
                            createSession();
                        }
                    }
                    else
                        updateUI("System said: unable to get public key");
                }
                catch (Exception ex) {
                    Log.e("Busted", ex.getMessage());
                }
            }
        }.execute();
    }

    private void createSession() {
        if(gotPublicKey) {
            // Produce message
            String encrypted = Crypto.doubleEncryptData("New Session".getBytes(), client.getPrivateKey(),
                    contact.getPublicKey());
            String encoded = Utility.encodeToBase64(encrypted.getBytes());
            // Send message
            sendSMSMessage(encoded, false);
            updateUI("System said: session request sent");
        }
        else
            updateUI("System said: unable to send session request");
    }

    private void setSecretKey(SecretKey key) {
        // Set secret key
        contact.setSessionKey(key);
        confirmedAES = true;
    }

    private void sessionResponse() {
        // Get contacts public key from pka
        createNewSession(true);
        if(gotPublicKey) {
            updateUI("Server said: Received session request, accepting...");
            // Get contact pub key
            PublicKey contactPubKey = contact.getPublicKey();
            // Produce message
            String encrypted = Crypto.doubleEncryptData("Session Accepted".getBytes(), client.getPrivateKey(),
                    contact.getPublicKey());
            // Encode
            String encoded = Utility.encodeToBase64(encrypted.getBytes());
            // Send message
            sendSMSMessage(encoded, false);
        }
    }

    private void produceSessionKey() {

        updateUI("Server said: Session requested approved, sending AES key");
        SecureRandom sc = new SecureRandom();
        byte[] aesBytes = new byte[16];
        sc.nextBytes(aesBytes);
        SecretKey k = Crypto.generateSecretKey(aesBytes);
        contact.setSessionKey(k);
        // Send it to contact
        // Get contact pub key
        PublicKey contactPubKey = contact.getPublicKey();
        // Produce message
        String encrypted = Crypto.doubleEncryptData(("AESKey:" + Utility.encodeToBase64(aesBytes)).getBytes(), client.getPrivateKey(),
                contact.getPublicKey());
        String encoded = Utility.encodeToBase64(encrypted.getBytes());
        // Send message
        sendSMSMessage(encoded, false);
        updateUI("System said: session key sent");
    }

    private String interceptMessage(String message) {

        String contents = null;

        // AES encrypted message
        if(confirmedAES && contact.getSessionKey() != null) {
            contents = Crypto.decodeAndDecryptAESMessage(message, contact.getSessionKey());
        }
        // Assume its an RSA message
        else {

            if(contact.getPublicKey() == null)
                createNewSession(true);

            byte[] decryptedBytes = Crypto.doubleDecryptData(message, contact.getPublicKey(), client.getPrivateKey());
            contents = new String(decryptedBytes);
        }

        return contents;
    }

    /**
     * public static Broadcast Receiver class which on receiving a text message
     * updates the clients Message list. Since Broadcast receivers do not have
     * a way of updating a UI from outside its class it has been implemented here.
     */
    public static class SMSBroadcastReceiver extends BroadcastReceiver {

        private MessagingActivity activity = null;

        public SMSBroadcastReceiver() {
        }

        public SMSBroadcastReceiver(MessagingActivity activity) {
            this.activity = activity;
        }

        public void onReceive(Context context, Intent intent) {  // obtain the SMS message
            Bundle bundle = intent.getExtras();
            StringBuilder stringBuilder = new StringBuilder();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdus.length; i++) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    // Get message body
                    String receivedString = message.getDisplayMessageBody();
                    String sender = message.getOriginatingAddress();
                    if(sender.equals("+64" + contactMobile.substring(1, contactMobile.length()))) {
                        // Add to builder
                        stringBuilder.append(receivedString);
                    }
                }

                String contents = stringBuilder.toString();
                contents = activity.interceptMessage(contents);

                switch(contents) {
                    case "New Session": {
                        activity.sessionResponse();
                        break;
                    }
                    case "Session Accepted": {
                        activity.produceSessionKey();
                        break;
                    }
                    default: {

                        if(contents.startsWith("AESKey:")) {
                            // Get aes key
                            String keyCode = contents.substring(7);
                            SecretKey key = Crypto.generateSecretKey(Utility.decodeFromBase64(keyCode));
                            activity.setSecretKey(key);
                        }
                        else
                            updateUI("They said: " + contents);
                        break;
                    }
                }
            } else {
                Toast.makeText(context, "No message body", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receivedBroadcastReceiver);
        super.onDestroy();
    }

    private void setContactPublicKey(PublicKey publicKey) {
        contact.setPublicKey(publicKey);
        gotPublicKey = true;
        updateUI("Server said: AES key received and stored");
    }
}


