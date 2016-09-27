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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static android.R.attr.absListViewStyle;
import static android.R.attr.value;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button requestEmailButton, connectButton;
    EditText yourNumberEditText, passwordEditText;
    private HttpHelper httpHelper;

    ClientHelper client;

    String password;

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

    public void setPasswordText()
    {
        passwordEditText.setText(password);
    }

    public boolean canSetPasswordText(String pass)
    {
        if(pass!=null||pass!="") {
            password = pass;
            return false; // false to exit the loop
        }
        return true;
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
                    while(canSetPasswordText(httpHelper.getResponse())) {
                        //wait for response from server for password
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    // Set Password when received response
                    String password = httpHelper.getResponse();
                    setPasswordText();
                    super.onPostExecute(aVoid);
                }
            }.execute();

            // Encrypt and Store data
            try {
                client = new ClientHelper(yourNumberEditText.getText().toString(),
                passwordEditText.getText().toString(), this);
                // Set up Join Request
                client.sendJoin();


            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


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
