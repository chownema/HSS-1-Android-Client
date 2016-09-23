package com.aut.android.highlysecuretexter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.HttpHelper;

import static android.R.attr.value;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button requestEmailButton, connectButton;
    private HttpHelper httpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);

        // Set buttons
        requestEmailButton = (Button) findViewById(R.id.email_request_button);
        requestEmailButton.setOnClickListener(this);

        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);

        // Set up HTTP Helper
        httpHelper = new HttpHelper(this);


    }

    @Override
    public void onClick(View view) {
        // If Requesting email
        if (view == requestEmailButton)
        {
            httpHelper.post("request/"+R.string.debug_mob_number);

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
