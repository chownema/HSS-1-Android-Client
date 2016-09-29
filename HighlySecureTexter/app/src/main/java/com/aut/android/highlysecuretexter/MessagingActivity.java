package com.aut.android.highlysecuretexter;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.Utility;

import java.util.ArrayList;

public class MessagingActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton sendMsgButton;
    EditText inputMessage, cipherMessage, inputNumber;
    ListView messageListView;
    Intent i;

    // Message List Var
    ArrayList<String> messageList;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Add Recipient Number to the title
        i = getIntent();
        setTitle(i.getStringExtra("number"));

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
        Utility.initDebugValues();
    }

    protected void sendSMSMessage()
    {
        String phoneNum = i.getStringExtra("number");
        try {
            // Break message into parts and send it
            SmsManager sms = SmsManager.getDefault();
            String msg = Utility.encryptAndEncodeString(inputMessage.getText().toString());
            ArrayList<String> parts = sms.divideMessage(msg);
            sms.sendMultipartTextMessage(phoneNum, null, parts, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent " + msg, Toast.LENGTH_LONG).show();

            // Add it to the list
            messageList.add("You :" + msg);
            adapter.notifyDataSetChanged();
            messageListView.smoothScrollToPosition(messageList.size(), messageList.size());
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),
                    "SMS failed, please try again.", Toast.LENGTH_LONG).show();
        }

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
                sendSMSMessage();
            }break;
        }
    }
}
