package com.aut.android.highlysecuretexter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.Client;
import com.aut.android.highlysecuretexter.Controller.Network;
import com.aut.android.highlysecuretexter.Controller.Utility;

import javax.crypto.SecretKey;

public class LoginActivity extends Activity implements View.OnClickListener {

    // Screen elements
    private Button connectButton, requestButton;
    private EditText mobileTextField, passwordTextField;

    // Public Strings
    private String mobile;
    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Set Edit Text
        mobileTextField = (EditText) findViewById(R.id.phone_edit_text);
        // Set buttons
        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);
        requestButton = (Button) findViewById(R.id.request_button);
        requestButton.setOnClickListener(this);

        // Get Permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS}, 1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS}, 1);

        Spinner staticSpinner = (Spinner) findViewById(R.id.static_spinner);

        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.numbers,
                        android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        staticSpinner.setAdapter(staticAdapter);
    }

    @Override
    public void onClick(View view) {
        // Connect to the server with the encrypted cipher array
        if (view == connectButton)
            connect();
        else if(view == requestButton)
            requestToJoin();
    }

    private void connect() {

        Toast.makeText(getApplicationContext(), "Connecting to PKA Server...", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try
                {
                    Network.connectToPKA(client);
                } catch (Exception e) {e.printStackTrace();}
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(client.isPkaConnected()) {
                    Toast.makeText(getApplicationContext(), "Successfully connected to PKA", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(LoginActivity.this, ContactsActivity.class);
                    myIntent.putExtra("client", client);
                    startActivity(myIntent);
                }
                else
                    Toast.makeText(getApplicationContext(), "Unable to connect to PKA", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void requestToJoin() {

        // Get one time password debug
        Toast.makeText(this, "Getting Ephemeral key...", Toast.LENGTH_SHORT).show();

        //mobile = mobileTextField.getText().toString();
        Spinner s = (Spinner) findViewById(R.id.static_spinner);
        mobile = s.getSelectedItem().toString();

        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... voids) {
                try
                {
                    SecretKey ephemeral = Network.getEphemeralKey(mobile);

                    if(ephemeral != null) {
                        // Create client and populate ephemeral key
                        setClient(new Client(mobile, ephemeral));
                    }
                }
                catch (Exception e) {e.printStackTrace();}
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if(client != null && client.getEphemeralKey() != null)
                    Toast.makeText(getApplicationContext(),
                            "Ephemeral has been received", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),
                            "Error - Ephemeral has not been received", Toast.LENGTH_SHORT).show();
            }

        }.execute();
    }

    private void setClient(Client temp) {
        this.client = temp;
    }
}
