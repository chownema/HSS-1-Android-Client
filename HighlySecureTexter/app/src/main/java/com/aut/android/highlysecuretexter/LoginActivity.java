package com.aut.android.highlysecuretexter;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.SMSBroadcastReceiver;
import com.aut.android.highlysecuretexter.Controller.Utility;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Screen elements
    private Button  connectButton;
    private EditText yourNumberEditText, passwordEditText;

    // Public Strings
    public String response;
    public String password;
    public String number;

    // Receiver
    private SMSBroadcastReceiver receivedBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Set Edit Text
        yourNumberEditText = (EditText) findViewById(R.id.phone_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);


        // Set buttons
        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);

        // Get one time password debug
        Toast.makeText(this, "DEBUG : Getting one time password..", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... voids) {
                try
                {
                    // Gets the first time password
                    password = Utility.getPassword("1");

                }
                catch (Exception e) {e.printStackTrace();}
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(),
                        password, Toast.LENGTH_SHORT).show();
                setPasswordText();
            }

        }.execute();

        // Get Permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS}, 1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS}, 1);

        receivedBroadcastReceiver = new SMSBroadcastReceiver();
        registerReceiver(receivedBroadcastReceiver,
                new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    public void onClick(View view) {
        // Connect to the server with the encrypted cipher array
        if (view == connectButton)
        {
            TelephonyManager tMgr = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            Toast.makeText(getApplicationContext(), "Connecting..." + password, Toast.LENGTH_SHORT).show();

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try
                    {

                        Utility.init("1", password);
                    } catch (Exception e) {e.printStackTrace();}
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                }
            }.execute();



            // Put If statement blocking for server response
            Intent myIntent = new Intent(LoginActivity.this, ContactsActivity.class);
            myIntent.putExtra("key", ""); //Optional parameters
            LoginActivity.this.startActivity(myIntent);
        }
    }

    /**
     * Setters
     */

    public void setPasswordText()
    {
        passwordEditText.setText(password);
    }
}
