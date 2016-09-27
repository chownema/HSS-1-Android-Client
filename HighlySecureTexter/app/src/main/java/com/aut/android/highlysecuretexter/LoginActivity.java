package com.aut.android.highlysecuretexter;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.ClientHelper;
import com.aut.android.highlysecuretexter.Controller.HttpHelper;

import static android.R.attr.absListViewStyle;
import static android.R.attr.value;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button requestEmailButton, connectButton;
    EditText yourNumberEditText, passwordEditText;
    private HttpHelper httpHelper;

    ClientHelper client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.login_activity);

        yourNumberEditText = (EditText) findViewById(R.id.phone_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);

        // Set buttons
        requestEmailButton = (Button) findViewById(R.id.email_request_button);
        requestEmailButton.setOnClickListener(this);

        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);

        // Set up HTTP Helper
        httpHelper = new HttpHelper(this);


    }


    public void setPasswordText(String pass)
    {
        passwordEditText.setText(pass);
    }


    @Override
    public void onClick(View view) {
        // If Requesting email
        if (view == requestEmailButton)
        {

            String number = yourNumberEditText.getText().toString();

            Toast.makeText(this, yourNumberEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            httpHelper.post("request/"+number);

            // Set password in password field
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    while(httpHelper.getPass() == null) {
                        //wait for response from server
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    String password = httpHelper.getPass();
                    setPasswordText(password);
                    // Encrpyt and Store data
                    client = new ClientHelper(yourNumberEditText.getText().toString(), password);
                    client.generateEphemeral();
                    client.generateKeys();
                    byte[] cipherBytes = client.encryptDetails();
                    Log.e("Size of cipher", ""+cipherBytes.length);
                    String bytesEncoded = Base64.encodeToString(cipherBytes, Base64.DEFAULT);
                    bytesEncoded = bytesEncoded.replace("+", "%2B");
                    bytesEncoded = bytesEncoded.replace("/", "%2F");


                    super.onPostExecute(aVoid);
                }
            }.execute();

        }
        if (view == connectButton)
        {
            Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();

            // Put If statement blocking for server response
            Intent myIntent = new Intent(LoginActivity.this, ContactsActivity.class);
            myIntent.putExtra("key", value); //Optional parameters
            LoginActivity.this.startActivity(myIntent);
        }
    }
}
