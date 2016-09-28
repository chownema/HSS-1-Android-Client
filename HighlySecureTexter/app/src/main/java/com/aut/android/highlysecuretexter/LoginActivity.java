package com.aut.android.highlysecuretexter;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.R.attr.value;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Screen elements
    private Button requestConnectionButton, connectButton;
    private EditText yourNumberEditText, passwordEditText;

    // Public Strings
    public String response;
    public String password;
    public String number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Set Edit Text
        yourNumberEditText = (EditText) findViewById(R.id.phone_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);

        // Set buttons
        requestConnectionButton = (Button) findViewById(R.id.email_request_button);
        requestConnectionButton.setOnClickListener(this);

        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == requestConnectionButton)
        {
            // Capture Phonenumber
            number = yourNumberEditText.getText().toString();

            // Set password in password field
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    response = "";
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    // Set Password when received response
                    Log.e("Password Returned", response);
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            }.execute();
        }

        // Connect to the server with the encrypted cipher array
        if (view == connectButton)
        {
            Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();

            // Put If statement blocking for server response
            Intent myIntent = new Intent(LoginActivity.this, ContactsActivity.class);
            myIntent.putExtra("key", value); //Optional parameters
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
